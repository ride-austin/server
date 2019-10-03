package com.rideaustin.report.entry;

import java.time.Instant;

import org.joda.time.Interval;

import com.rideaustin.report.ReportField;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TripFulfillmentReportEntry {

  private final Interval interval;
  @ReportField(order = 3)
  private final long completed;

  @ReportField(order = 4)
  private final long riderCancelled;

  @ReportField(order = 5)
  private final long driverCancelled;

  @ReportField(order = 6)
  private final long adminCancelled;

  @ReportField(order = 7)
  private final long noDriverAvailable;

  @ReportField(order = 1)
  public Instant getStartDate() {
    return Instant.ofEpochMilli(interval.getStartMillis());
  }

  @ReportField(order = 2)
  public Instant getEndDate() {
    return Instant.ofEpochMilli(interval.getEndMillis());
  }

}
