package com.rideaustin.service.user;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.model.City;
import com.rideaustin.service.email.AbstractTemplateEmail;

public class DonateEmail extends AbstractTemplateEmail {

  private static final String TEMPLATE = "donate_notification.ftl";

  public DonateEmail(String recipient, String fullName, String email, String address, Double lat, Double lng, String comment, City city) {
    super("New donation request", TEMPLATE);
    ImmutableMap.Builder<String, Object> modelBuilder = ImmutableMap.<String, Object>builder()
      .put("city", city)
      .put("fullName", fullName)
      .put("email", email)
      .put("address", address)
      .put("location", String.format("%.7f,%.7f", lat, lng));
    if (comment != null) {
      modelBuilder.put("comment", comment);
    }
    setModel(modelBuilder.build());
    addRecipient(recipient);
  }
}
