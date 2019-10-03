package com.rideaustin.service.email;

import javax.annotation.Nonnull;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.rideaustin.model.City;
import com.rideaustin.model.lostandfound.LostItemInfo;

public class LostItemEmail extends AbstractTemplateEmail {

  private static final String LOST_ITEM_EMAIL_TEMPLATE = "lost_item_template.ftl";

  public LostItemEmail(LostItemInfo lostItemInfo, @Nonnull String description, @Nonnull String details,
    String phoneNumber, City city) {
    super("I lost an item", LOST_ITEM_EMAIL_TEMPLATE);
    setCharset(Charsets.UTF_16.displayName());
    setModel(ImmutableMap.<String, Object>builder()
      .put("description", description)
      .put("details", details)
      .put("rideId", lostItemInfo.getRideId())
      .put("driverEmail", lostItemInfo.getDriverEmail())
      .put("driverName", lostItemInfo.getDriverName())
      .put("riderId", lostItemInfo.getRiderId())
      .put("riderEmail", lostItemInfo.getRiderEmail())
      .put("riderName", lostItemInfo.getRiderName())
      .put("riderPhone", phoneNumber)
      .put("city", city)
      .build());
    addRecipients(city.getSupportEmail());
  }

}
