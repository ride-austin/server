package com.rideaustin.service.user;

import javax.annotation.Nonnull;

import org.apache.commons.mail.EmailException;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.Constants;
import com.rideaustin.model.City;
import com.rideaustin.model.user.Driver;
import com.rideaustin.service.email.AbstractTemplateEmail;

public class DriverSignupEmail extends AbstractTemplateEmail {

  public static final String TITLE = Constants.EmailTitle.DRIVER_SIGNUP_EMAIL;
  private static final String TEMPLATE = "driver_signup_%s.ftl";

  public DriverSignupEmail(Driver driver, @Nonnull City city, String payoneerLink) throws EmailException {
    super(city.getAppName() + TITLE, String.format(TEMPLATE, city.getName().toLowerCase()));
    setModel(ImmutableMap.of("driver", driver, "payoneerLink", payoneerLink, "city", city));
    addRecipient(driver.getUser().getFullName(), driver.getUser().getEmail());
    setFrom(city.getContactEmail(), city.getAppName());
  }

}
