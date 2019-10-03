package com.rideaustin.report.entry;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.rideaustin.report.FormatAs;
import com.rideaustin.report.ReportField;

import lombok.Getter;

@Getter
public class UniqueRidersDriversReportEntry {
  @ReportField(order = 1, format = FormatAs.DATE, name = "Last day of week")
  private final LocalDate endOfWeek;
  @ReportField(order = 2)
  private final Long totalDriversSignedUp;
  @ReportField(order = 3)
  private final Long driversSignedUpThisWeek;
  @ReportField(order = 4, name = "First-time drivers")
  private final Long firstTimeDrivers;
  @ReportField(order = 5)
  private final Long uniqueDrivers;
  @ReportField(order = 6)
  private final Long totalRidersSignedUp;
  @ReportField(order = 7)
  private final Long ridersSignedUpThisWeek;
  @ReportField(order = 8, name = "First-time riders")
  private final Long firstTimeRiders;
  @ReportField(order = 9)
  private final Long uniqueRiders;
  @ReportField(order = 10)
  private final BigDecimal hoursOnline;
  @ReportField(order = 11)
  private final BigDecimal hoursDriven;

  public UniqueRidersDriversReportEntry(LocalDate endOfWeek, Long totalDriversSignedUp, Long driversSignedUpThisWeek,
    Long uniqueDrivers, Long totalRidersSignedUp, Long ridersSignedUpThisWeek, Long uniqueRiders, Long firstTimeDrivers,
    Long firstTimeRiders, BigDecimal hoursDriven, BigDecimal hoursOnline) {
    this.endOfWeek = endOfWeek;
    this.totalDriversSignedUp = totalDriversSignedUp;
    this.driversSignedUpThisWeek = driversSignedUpThisWeek;
    this.uniqueDrivers = uniqueDrivers;
    this.totalRidersSignedUp = totalRidersSignedUp;
    this.ridersSignedUpThisWeek = ridersSignedUpThisWeek;
    this.uniqueRiders = uniqueRiders;
    this.firstTimeDrivers = firstTimeDrivers;
    this.firstTimeRiders = firstTimeRiders;
    this.hoursDriven = hoursDriven;
    this.hoursOnline = hoursOnline;
  }
}
