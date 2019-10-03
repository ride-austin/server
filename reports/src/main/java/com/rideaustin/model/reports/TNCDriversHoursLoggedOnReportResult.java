package com.rideaustin.model.reports;

import java.time.Instant;

import com.querydsl.core.Tuple;
import com.rideaustin.report.TupleConsumer;

public class TNCDriversHoursLoggedOnReportResult implements TupleConsumer {

  private final long id;
  private final Instant createdDate;
  private final Instant locationUpdatedDate;

  public TNCDriversHoursLoggedOnReportResult(Tuple tuple) {
    this.id = getLong(tuple, 2);
    this.createdDate = getInstantFromTimestamp(tuple, 0);
    this.locationUpdatedDate = getInstantFromTimestamp(tuple, 1);
  }

  public long getId() {
    return id;
  }

  public Instant getCreatedDate() {
    return createdDate;
  }

  public Instant getLocationUpdatedDate() {
    return locationUpdatedDate;
  }
}
