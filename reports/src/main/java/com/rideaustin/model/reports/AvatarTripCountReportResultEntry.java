package com.rideaustin.model.reports;

import java.time.Instant;

import com.querydsl.core.Tuple;
import com.rideaustin.report.TupleConsumer;

import lombok.Getter;

@Getter
public class AvatarTripCountReportResultEntry implements TupleConsumer {

  private final Long rideId;
  private final Long avatarId;
  private final Instant completedOn;
  private final String firstName;
  private final String lastName;
  private final String email;

  public AvatarTripCountReportResultEntry(Tuple tuple) {
    int index = 0;
    rideId = getLong(tuple, index++);
    avatarId = getLong(tuple, index++);
    completedOn = getInstantFromTimestamp(tuple, index++);
    firstName = getString(tuple, index++);
    lastName = getString(tuple, index++);
    email = getString(tuple, index);
  }

}
