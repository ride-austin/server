package com.rideaustin.service.user;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.commons.mail.EmailException;

import com.rideaustin.model.City;
import com.rideaustin.model.user.Driver;
import com.rideaustin.service.email.AbstractTemplateEmail;

public class ReferADriverEmail extends AbstractTemplateEmail {

  private static final String TITLE = "Hello from ";
  private static final String TEMPLATE = "refer_a_driver.ftl";

  public ReferADriverEmail(Driver driver, @Nonnull City city, String recipient, String fromEmail, String fromName) throws EmailException {
    super(TITLE + city.getAppName() + "!", TEMPLATE);
    Map<String, Object> model = new HashMap<>();

    fillDriverDetails(model, driver, city);
    setFrom(fromEmail, fromName);
    addRecipient(recipient);
    setModel(model);
  }

  private void fillDriverDetails(Map<String, Object> model, Driver driver, City city) {
    model.put("driverFullName", driver.getUser().getFullName());
    model.put("playStoreLink", city.getPlayStoreLink());
    model.put("appStoreLink", city.getAppStoreLink());
    model.put("city", city);
  }
}
