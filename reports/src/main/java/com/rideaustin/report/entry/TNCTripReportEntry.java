package com.rideaustin.report.entry;

import com.rideaustin.report.ReportField;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TNCTripReportEntry {

  @ReportField(order = 1)
  private String reportingMonth;
  @ReportField(order = 2)
  private String timeBlock;
  @ReportField(order = 3)
  private String zipCode;
  @ReportField(order = 4)
  private long requests;
  @ReportField(order = 5)
  private long requestsNotServiced;
  @ReportField(order = 6)
  private long pickups;
  @ReportField(order = 7)
  private long dropoffs;
  @ReportField(order = 8)
  private long accessibleRequestsServiced;
  @ReportField(order = 9)
  private long accessibleRequestsNotServiced;
}
