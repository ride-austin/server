package com.rideaustin.report.entry;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import com.querydsl.core.Tuple;
import com.rideaustin.report.ReportField;
import com.rideaustin.report.TupleConsumer;

import lombok.Getter;

@Getter
public class PromocodeUsageReportEntry implements TupleConsumer {

  @ReportField(order = 1, name = "Rider ID")
  private final Long riderId;
  @ReportField(order = 2)
  private final String firstName;
  @ReportField(order = 3)
  private final String lastName;
  @ReportField(order = 4)
  private final String email;
  @ReportField(order = 5)
  private final LocalDate firstTripCompletedOn;
  @ReportField(order = 6)
  private final Long tripsCompleted;

  public PromocodeUsageReportEntry(Tuple tuple) {
    int index = 0;
    this.riderId = getLong(tuple, index++);
    this.firstName = getString(tuple, index++);
    this.lastName = getString(tuple, index++);
    this.email = getString(tuple, index++);
    this.firstTripCompletedOn = Optional.ofNullable(getLocalDateTimeFromTimestamp(tuple, index++))
      .map(LocalDateTime::toLocalDate).orElse(null);
    this.tripsCompleted = getLong(tuple, index);
  }

}
