package com.rideaustin.model.reports;

import java.time.Instant;

import com.querydsl.core.Tuple;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.report.TupleConsumer;

public class TripFulfillmentQueryResultEntry implements TupleConsumer {

  private Instant createdOn;
  private Instant completedOn;
  private Instant cancelledOn;
  private RideStatus status;

  public TripFulfillmentQueryResultEntry(Instant createdOn, Instant completedOn, Instant cancelledOn, RideStatus status) {
    this.createdOn = createdOn;
    this.completedOn = completedOn;
    this.cancelledOn = cancelledOn;
    this.status = status;
  }

  public TripFulfillmentQueryResultEntry(Tuple tuple) {
    this.createdOn = getInstantFromTimestamp(tuple, 0);
    this.completedOn = getInstantFromTimestamp(tuple, 1);
    this.cancelledOn = getInstantFromTimestamp(tuple, 2);
    this.status = get(tuple, 3, RideStatus.class);
  }

  public Instant getCreatedOn() {
    return createdOn;
  }

  public Instant getCompletedOn() {
    return completedOn;
  }

  public Instant getCancelledOn() {
    return cancelledOn;
  }

  public RideStatus getStatus() {
    return status;
  }
}
