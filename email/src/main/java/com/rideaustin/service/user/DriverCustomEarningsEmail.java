package com.rideaustin.service.user;

import java.time.LocalDate;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.mail.EmailException;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.Constants;
import com.rideaustin.model.City;
import com.rideaustin.service.email.AbstractTemplateEmail;
import com.rideaustin.service.model.DriverCustomEarnings;

public class DriverCustomEarningsEmail extends AbstractTemplateEmail {

  private static final String TEMPLATE = "driver_additional_earnings.ftl";

  public DriverCustomEarningsEmail(DriverCustomEarnings driverCustomEarnings,
    @Nullable List<String> recipients,
    LocalDate reportLocalDate,
    City city) throws EmailException {
    super(null, TEMPLATE);

    String email = driverCustomEarnings.getDriver().getEmail();
    if (recipients == null) {
      addRecipient(email);
    } else {
      addRecipients(recipients);
    }
    setModel(ImmutableMap.of("earnings", driverCustomEarnings, "city", city));
    String subject = "Additional earnings report for " + Constants.DATE_FORMATTER.format(reportLocalDate);
    this.setSubject(subject);
    setFrom(city.getContactEmail(), city.getAppName());
  }

}
