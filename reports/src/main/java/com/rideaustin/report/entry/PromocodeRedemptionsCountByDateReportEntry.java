package com.rideaustin.report.entry;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.querydsl.core.Tuple;
import com.rideaustin.Constants;
import com.rideaustin.report.ReportField;
import com.rideaustin.report.TupleConsumer;

public class PromocodeRedemptionsCountByDateReportEntry implements TupleConsumer {

  private final LocalDate date;
  private final Long redemptionsCount;

  public PromocodeRedemptionsCountByDateReportEntry(Tuple tuple) {
    int index = 0;
    this.date = LocalDateTime.ofInstant(getInstantFromDate(tuple, index++), Constants.CST_ZONE).toLocalDate();
    this.redemptionsCount = getLong(tuple, index);
  }

  @ReportField(order = 1)
  public LocalDate getDate() {
    return date;
  }

  @ReportField(order = 2)
  public Long getRedemptionsCount() {
    return redemptionsCount;
  }
}
