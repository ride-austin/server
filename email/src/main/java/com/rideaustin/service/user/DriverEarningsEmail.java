package com.rideaustin.service.user;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.mail.EmailException;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.Constants;
import com.rideaustin.model.City;
import com.rideaustin.service.email.AbstractTemplateEmail;
import com.rideaustin.service.model.DriverEarnings;

public class DriverEarningsEmail extends AbstractTemplateEmail {

  private static final String TEMPLATE = "driver_earnings.ftl";

  public DriverEarningsEmail(@Nonnull DriverEarnings earnings, @Nullable List<String> recipients, City city) throws EmailException {
    super(null, TEMPLATE);

    String email = earnings.getDriver().getEmail();
    if (recipients == null) {
      addRecipient(email);
    } else {
      addRecipients(recipients);
    }
    setModel(ImmutableMap.of("earnings", earnings, "city", city));
    String subject = "Daily earnings report for " + Constants.DATE_FORMATTER.format(earnings.getReportLocalDate());
    this.setSubject(subject);
    setFrom(city.getContactEmail(), city.getAppName());
  }

}
