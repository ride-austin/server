package com.rideaustin.service.user;

import org.apache.commons.mail.EmailException;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.model.City;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.DriverEmailReminder;
import com.rideaustin.service.email.AbstractTemplateEmail;

public class DriverReminderEmail extends AbstractTemplateEmail {
  public DriverReminderEmail(DriverEmailReminder reminder,  String content, String subject, Driver driver, City city) throws EmailException {
    super(reminder.getSubject().isEmpty() ? subject : reminder.getSubject(), reminder.getEmailTemplate());
    ImmutableMap.Builder<String, Object> modelBuilder = ImmutableMap.<String, Object>builder()
      .put("driver", driver)
      .put("city", city);
    if (content != null) {
      modelBuilder.put("content", content);
    }
    setModel(modelBuilder.build());
    addRecipient(driver.getEmail());
    setFrom(city.getEmailAddresses().get(reminder.getEmailType()), city.getAppName());
  }

}
