package com.rideaustin.jobs;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import javax.inject.Inject;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.stereotype.Component;

import com.rideaustin.Constants;
import com.rideaustin.model.City;
import com.rideaustin.model.CustomPayment;
import com.rideaustin.model.user.Driver;
import com.rideaustin.service.CustomPaymentService;
import com.rideaustin.service.DriverService;
import com.rideaustin.service.model.DriverCustomEarnings;
import com.rideaustin.service.user.DriverCustomEarningsEmail;

import lombok.Setter;

@Component
public class CustomPaymentsEmailJob extends BaseEmailJob {

  @Setter(onMethod = @__(@Inject))
  private CustomPaymentService customPaymentService;

  @Setter(onMethod = @__(@Inject))
  private DriverService driverService;

  @Override
  protected String getDescription() {
    return "custom payment email";
  }

  @Override
  protected void executeInternal() throws JobExecutionException {
    LocalDate effectiveReportDate = reportDate == null ? LocalDate.now(Constants.CST_ZONE) : reportDate;
    DayOfWeek dayOfWeek = DayOfWeek.of(effectiveReportDate.getDayOfWeek().getValue());
    LocalDate adjustedReportDate = (!dayOfWeek.equals(DayOfWeek.MONDAY)) ?
      effectiveReportDate.with(TemporalAdjusters.previous(DayOfWeek.MONDAY)) : effectiveReportDate;

    try {
      Driver driver = driverService.findDriver(driverId);
      City city = cityService.getById(driver.getCityId());

      List<CustomPayment> driverCustomPayments =
        customPaymentService.getWeeklyCustomPaymentsForDriver(effectiveReportDate, adjustedReportDate, driverId);

      DriverCustomEarnings earnings =
        new DriverCustomEarnings(adjustedReportDate, Constants.CST_ZONE, driver, sumPayments(driverCustomPayments), driverCustomPayments);

      emailService.sendEmail(new DriverCustomEarningsEmail(earnings, recipients, adjustedReportDate, city));
    } catch (Exception e) {
      throw new JobExecutionException(e);
    }
  }

  private Money sumPayments(List<CustomPayment> payments) {
    return payments.stream().map(CustomPayment::getValue).reduce(Money.zero(CurrencyUnit.USD), Money::plus);
  }
}
