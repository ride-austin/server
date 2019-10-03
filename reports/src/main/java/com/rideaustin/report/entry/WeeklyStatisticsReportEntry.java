package com.rideaustin.report.entry;

import java.math.BigDecimal;

import com.rideaustin.report.ReportField;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WeeklyStatisticsReportEntry {

  @ReportField(order = 1)
  private final String week;
  @ReportField(order = 2)
  private final String carCategory;
  @ReportField(order = 3, name = "# regular trips")
  private final Long regularTripsCount;
  @ReportField(order = 4, name = "# priority fare trips")
  private final Long priorityTripsCount;
  @ReportField(order = 5)
  private final BigDecimal grossFare;
  @ReportField(order = 6)
  private final BigDecimal surgeFare;
  @ReportField(order = 7, name = "Charity round-up")
  private final BigDecimal charityRoundUp;
  @ReportField(order = 8, name = "Driver payment (excl. tips, priority fare)")
  private final BigDecimal driverPayment;
  @ReportField(order = 9)
  private final BigDecimal cancellationFees;
  @ReportField(order = 10)
  private final BigDecimal tips;
  @ReportField(order = 11)
  private final BigDecimal priorityFare;
  @ReportField(order = 12)
  private final BigDecimal milesDriven;

}
