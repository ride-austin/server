package com.rideaustin.service.user;

import org.apache.commons.mail.EmailException;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.model.City;
import com.rideaustin.model.user.Driver;
import com.rideaustin.service.email.AbstractTemplateEmail;

public class LicenseUpdatedEmail extends AbstractTemplateEmail {

  public static final String TEMPLATE = "license_updated.ftl";
  public static final String TITLE = "Driver license has been updated";

  public LicenseUpdatedEmail(Driver driver, City city) throws EmailException {
    super(TITLE, TEMPLATE);
    setModel(ImmutableMap.of("driver", driver, "city", city));
    addRecipient(String.format("%s Support", city.getAppName()), city.getSupportEmail());
    setFrom(city.getContactEmail(), city.getAppName());
  }
}
