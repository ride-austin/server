package com.rideaustin.service.user;

import javax.annotation.Nonnull;

import org.apache.commons.mail.EmailException;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.model.City;
import com.rideaustin.model.user.Driver;
import com.rideaustin.service.email.AbstractTemplateEmail;

public class LicenseExpiredEmail extends AbstractTemplateEmail {

  private static final String TITLE = "Your driver license is about to expire";
  private static final String TEMPLATE = "driver_license_expire.ftl";

  public LicenseExpiredEmail(@Nonnull Driver driver, @Nonnull City city) throws EmailException {
    super(TITLE, TEMPLATE);
    setModel(ImmutableMap.of("driver", driver, "city", city));
    addRecipient(driver.getUser().getFullName(), driver.getUser().getEmail());
    setFrom(city.getContactEmail(), city.getAppName());
  }

}
