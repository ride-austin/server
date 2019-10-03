package com.rideaustin.model.reports;

import java.time.Instant;

import com.querydsl.core.Tuple;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.report.TupleConsumer;

public class TNCTripReportResult implements TupleConsumer {

  private final Instant createdDate;
  private final RideStatus rideStatus;
  private final String startZipCode;
  private final String endZipCode;
  private final Instant completedOn;

  public TNCTripReportResult(Tuple tuple) {
    this.createdDate = getInstantFromTimestamp(tuple, 0);
    this.rideStatus = get(tuple, 1, RideStatus.class);
    this.startZipCode = getString(tuple, 2);
    this.endZipCode = getString(tuple, 3);
    this.completedOn = getInstantFromTimestamp(tuple, 4);
  }

  public Instant getCreatedDate() {
    return createdDate;
  }

  public RideStatus getRideStatus() {
    return rideStatus;
  }

  public String getStartZipCode() {
    return startZipCode;
  }

  public String getEndZipCode() {
    return endZipCode;
  }

  public Instant getCompletedOn() {
    return completedOn;
  }
}
