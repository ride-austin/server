package com.rideaustin.report.entry;

import java.time.LocalDate;
import java.time.LocalTime;

import com.querydsl.core.Tuple;
import com.rideaustin.report.ReportField;

public class PromocodeRedemptionsCountByHourReportEntry extends PromocodeRedemptionsCountByDateReportEntry {

  private final LocalTime time;

  public PromocodeRedemptionsCountByHourReportEntry(Tuple tuple) {
    super(tuple);
    this.time = LocalTime.of(getInteger(tuple, 2), 0);
  }

  @ReportField(order = 1)
  @Override
  public LocalDate getDate() {
    return super.getDate();
  }

  @ReportField(order = 2)
  public LocalTime getTime() {
    return time;
  }

  @ReportField(order = 3)
  @Override
  public Long getRedemptionsCount() {
    return super.getRedemptionsCount();
  }
}
