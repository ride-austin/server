package com.rideaustin.service.user;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.mail.EmailException;

import com.google.common.base.Charsets;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.City;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.User;
import com.rideaustin.service.email.AbstractTemplateEmail;
import com.rideaustin.utils.FormatUtils;

public class SupportMessageEmail extends AbstractTemplateEmail {

  private static final String TITLE = " Customer Support Request";
  private static final String TEMPLATE = "support_message.ftl";

  public SupportMessageEmail(User user, Ride ride, Rider rider, Driver driver, String message, String from, String fromName, String recipient, City city, ClientType clientType) throws EmailException, AddressException {
    super(city.getAppName() + TITLE, TEMPLATE);
    setCharset(Charsets.UTF_16.displayName());
    Map<String, Object> model = new HashMap<>();

    fillUserDetails(model, user, clientType);
    fillRideDetails(model, ride, rider, driver);
    model.put("message", message);
    model.put("city", city);
    setFrom(from, fromName);
    InternetAddress replyToAddress = new InternetAddress(user.getEmail());
    setReplyTo(Collections.singletonList(replyToAddress));
    setModel(model);
    addRecipient("RA Support", recipient);
  }

  private void fillUserDetails(Map<String, Object> model, User user, ClientType clientType) {
    model.put("userFullName", user.getFullName());
    model.put("userEmail", user.getEmail());
    model.put("application", clientType.toString());
  }

  private void fillRideDetails(Map<String, Object> model, Ride ride, Rider rider, Driver driver) {
    model.put("isRidePresent", ride != null);
    if (ride != null) {
      model.put("rideId", String.valueOf(ride.getId()));
      model.put("rideStatus", ride.getStatus());
      model.put("rideStartedOn", Optional.ofNullable(ride.getStartedOn()).map(FormatUtils::formatDateTime).orElse(null));
      model.put("rideCompletedOn", Optional.ofNullable(ride.getCompletedOn()).map(FormatUtils::formatDateTime).orElse(null));
      fillDriverDetails(model, driver);
      fillRiderDetails(model, rider);
    }
  }

  private void fillRiderDetails(Map<String, Object> model, Rider rider) {
    if (rider != null) {
      model.put("isRiderPresent", true);
      model.put("riderFullName", rider.getUser().getFullName());
      model.put("riderEmail", rider.getUser().getEmail());
      model.put("riderId", String.valueOf(rider.getId()));
    }
  }

  private void fillDriverDetails(Map<String, Object> model, Driver driver) {
    if (driver != null) {
      model.put("isDriverPresent", true);
      model.put("driverFullName", driver.getUser().getFullName());
      model.put("driverEmail", driver.getUser().getEmail());
      model.put("driverId", String.valueOf(driver.getId()));
    }
  }

}
