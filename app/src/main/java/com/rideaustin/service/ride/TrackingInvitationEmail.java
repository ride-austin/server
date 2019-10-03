package com.rideaustin.service.ride;

import javax.annotation.Nonnull;

import org.apache.commons.mail.EmailException;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.model.City;
import com.rideaustin.model.user.User;
import com.rideaustin.service.email.AbstractTemplateEmail;

public class TrackingInvitationEmail extends AbstractTemplateEmail {

  private static final String TITLE = " Real Time Tracking Invitation";
  private static final String TEMPLATE = "tracking_invitation.ftl";

  public TrackingInvitationEmail(User user, @Nonnull City city, String url, String recipient) throws EmailException {
    super(city.getAppName() + TITLE, TEMPLATE);

    setModel(ImmutableMap.of("user", user, "url", url, "city", city));
    addRecipient(recipient, recipient);
    setFrom(city.getContactEmail(), city.getAppName());
  }

}
