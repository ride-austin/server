package com.rideaustin.jobs;

import javax.inject.Inject;

import org.apache.commons.mail.EmailException;
import org.springframework.stereotype.Component;

import com.rideaustin.service.payment.EarningsReportingService;
import com.rideaustin.service.user.DriverEarningsEmail;

import lombok.Setter;

@Component
public class EarningsEmailJob extends BaseEmailJob {

  @Setter(onMethod = @__(@Inject))
  private EarningsReportingService earningsReportingService;

  @Override
  protected String getDescription() {
    return "earnings email";
  }

  @Override
  protected void executeInternal() {

    cityService.findAll().forEach(city -> earningsReportingService.collectEarnings(reportDate, driverId, city)
      .forEach(earnings -> {
        try {
          emailService.sendEmail(new DriverEarningsEmail(earnings, recipients, city));
        } catch (EmailException e) {
          throw new IllegalArgumentException(e);
        }
      }));
  }
}
