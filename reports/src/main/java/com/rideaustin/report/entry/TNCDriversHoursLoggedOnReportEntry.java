package com.rideaustin.report.entry;

import java.time.LocalDate;

import com.rideaustin.report.ReportField;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TNCDriversHoursLoggedOnReportEntry {

  @ReportField(order = 1)
  private LocalDate date;
  @ReportField(order = 2)
  private double driverHoursLoggedOn;
  @ReportField(order = 3, name = "Number of Drivers Logged On (Daily)")
  private int numberOfDrivers;

}
