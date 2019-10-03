package com.rideaustin.service.user;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.mail.EmailException;

import com.google.common.base.Charsets;
import com.rideaustin.model.City;
import com.rideaustin.model.SupportTopic;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.User;
import com.rideaustin.service.email.AbstractTemplateEmail;
import com.rideaustin.utils.FormatUtils;

public class DefaultSupportMessageEmail extends AbstractTemplateEmail {

  private static final String TITLE = " %s - Customer Support Request";
  private static final String TEMPLATE = "default_support_message.ftl";
  private static final String APPLICATION = "application";

  public DefaultSupportMessageEmail(User user, AvatarType avatarType, City city, Ride ride, String managementConsoleUrl,
    SupportTopic topic, SupportTopic subTopic, String message) throws EmailException, AddressException {
    super(String.format(TITLE, city.getAppName()), TEMPLATE);
    setCharset(Charsets.UTF_16.displayName());
    Map<String, Object> model = new HashMap<>();

    String from = String.format("%s Support", city.getAppName());
    fillUserDetails(model, user);
    fillRideDetails(model, ride, managementConsoleUrl);
    fillTopicDetails(model, topic, subTopic);
    model.put("message", message);
    model.put("city", city);
    switch (avatarType) {
      case DRIVER:
        model.put(APPLICATION, "Driver");
        break;
      case RIDER:
        model.put(APPLICATION, "Rider");
        break;
      default:
        model.put(APPLICATION, "Console");
    }
    String topicDescription = subTopic != null ? subTopic.getDescription() : topic.getDescription();
    setSubject(String.format("%s - %s", model.get(APPLICATION).toString().toUpperCase(), topicDescription));
    InternetAddress replyToAddress = new InternetAddress(user.getEmail());
    setReplyTo(Collections.singletonList(replyToAddress));
    setFrom(city.getContactEmail(), city.getAppName());
    setCc(Collections.singletonList(replyToAddress));
    setModel(model);
    addRecipient(from, city.getSupportEmail());

  }

  private void fillTopicDetails(Map<String, Object> model, SupportTopic topic, SupportTopic subTopic) {
    model.put("topic", topic.getDescription());
    if (subTopic != null) {
      model.put("subTopic", subTopic.getDescription());
    }
  }

  private void fillUserDetails(Map<String, Object> model, User user) {
    model.put("userFullName", user.getFullName());
    model.put("userEmail", user.getEmail());
  }

  private void fillRideDetails(Map<String, Object> model, Ride ride, String managementConsoleUrl) {
    model.put("isRidePresent", ride != null);
    if (ride != null) {
      String id = String.valueOf(ride.getId());
      model.put("rideId", id);
      model.put("rideDate", FormatUtils.formatDateTime(ride.getStartedOn() == null ? ride.getCreatedDate() : ride.getStartedOn()));
      model.put("rideDetailsLink", String.format(managementConsoleUrl.concat("/ride/%s"), id));
    }
  }

}
