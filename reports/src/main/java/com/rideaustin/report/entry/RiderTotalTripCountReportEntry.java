package com.rideaustin.report.entry;

import java.time.LocalDateTime;

import com.querydsl.core.Tuple;
import com.rideaustin.report.FormatAs;
import com.rideaustin.report.ReportField;
import com.rideaustin.report.TupleConsumer;

import lombok.Getter;

@Getter
public class RiderTotalTripCountReportEntry implements TupleConsumer {

  @ReportField(order = 1, name = "Rider ID")
  private final Long riderId;
  @ReportField(order = 2)
  private final String firstName;
  @ReportField(order = 3)
  private final String lastName;
  @ReportField(order = 4)
  private final String email;
  @ReportField(order = 5)
  private final Long totalTripCount;
  @ReportField(order = 6, format = FormatAs.DATETIME)
  private final LocalDateTime dateOfFirstTrip;

  public RiderTotalTripCountReportEntry(Tuple tuple) {
    int index = 0;
    this.riderId = getLong(tuple, index++);
    this.firstName = getString(tuple, index++);
    this.lastName = getString(tuple, index++);
    this.email = getString(tuple, index++);
    this.dateOfFirstTrip = getLocalDateTimeFromTimestamp(tuple, index++);
    this.totalTripCount = getLong(tuple, index);
  }

}
