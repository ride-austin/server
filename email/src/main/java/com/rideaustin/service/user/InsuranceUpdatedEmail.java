package com.rideaustin.service.user;

import org.apache.commons.mail.EmailException;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.model.City;
import com.rideaustin.model.ride.Car;
import com.rideaustin.model.user.Driver;
import com.rideaustin.service.email.AbstractTemplateEmail;

public class InsuranceUpdatedEmail extends AbstractTemplateEmail {
  public static final String TEMPLATE = "insurance_updated.ftl";
  public static final String TITLE = "Insurance has been updated";

  public InsuranceUpdatedEmail(Driver driver, Car car, City city) throws EmailException {
    super(TITLE, TEMPLATE);
    setModel(ImmutableMap.of("driver", driver, "car", car, "city", city));
    addRecipient(String.format("%s Support", city.getAppName()), city.getSupportEmail());
    setFrom(city.getContactEmail(), city.getAppName());
  }
}
