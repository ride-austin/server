package com.rideaustin.service.model;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.joda.money.Money;

import com.rideaustin.model.CustomPayment;
import com.rideaustin.model.user.Driver;
import com.rideaustin.utils.DateUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriverCustomEarnings {

  private LocalDate reportDate;
  private ZoneId zoneId;
  private Driver driver;
  private Money sumOfEarnings;
  private List<CustomPayment> customPayments;

  public DriverCustomEarnings(LocalDate reportDate, ZoneId zoneId, Driver driver, Money sumOfEarnings, List<CustomPayment> customPayments) {
    this.reportDate = reportDate;
    this.zoneId = zoneId;
    this.driver = driver;
    this.sumOfEarnings = sumOfEarnings;
    this.customPayments = customPayments;
  }

  public Date getReportDate() {
    return DateUtils.localDateToDate(reportDate, zoneId);
  }

}
