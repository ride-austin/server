package com.rideaustin.service.email;

import java.util.Date;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.rideaustin.model.City;
import com.rideaustin.model.user.Driver;
import com.rideaustin.utils.FormatUtils;

public class FoundItemEmail extends AbstractTemplateEmail {

  private static final String FOUND_ITEM_EMAIL_TEMPLATE = "found_item_template.ftl";

  public FoundItemEmail(Long rideId, Driver driver, Date foundOn, String rideDescription, String details, boolean sharingContactsAllowed, @Nullable String url, City city) {
    super("I found an item", FOUND_ITEM_EMAIL_TEMPLATE);
    setCharset(Charsets.UTF_16.displayName());
    addRecipients(city.getSupportEmail());
    setModel(
      ImmutableMap.<String, Object>builder()
        .put("rideId", Optional.ofNullable(rideId).map(String::valueOf).orElse("N/A"))
        .put("driverId", driver.getId())
        .put("driverEmail", driver.getEmail())
        .put("driverName", driver.getFullName())
        .put("driverPhone", driver.getPhoneNumber())
        .put("foundOn", FormatUtils.formatDateTime(foundOn))
        .put("rideInfo", rideDescription)
        .put("itemDetails", details)
        .put("sharingAllowed", sharingContactsAllowed)
        .put("url", Optional.ofNullable(url).orElse(""))
        .put("city", city)
        .build()
    );
  }
}
