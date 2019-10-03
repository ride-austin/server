package com.rideaustin.service.user;

import java.util.HashMap;
import java.util.Map;

import com.rideaustin.model.City;
import com.rideaustin.model.user.Driver;
import com.rideaustin.service.thirdparty.AbstractTemplateSMS;

public class ReferADriverSMS extends AbstractTemplateSMS {

  private static final String TEMPLATE = "refer_a_driver_sms.ftl";

  public ReferADriverSMS(Driver driver, City city, String recipient) {
    super(TEMPLATE);
    Map<String, Object> model = new HashMap<>();

    fillDriverDetails(model, driver, city);
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
