package com.rideaustin.model.enums;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum RideStatus {
  REQUEST_QUEUED,
  REQUEST_QUEUE_EXPIRED,
  REQUESTED,
  RIDER_CANCELLED,
  DRIVER_ASSIGNED,
  DRIVER_CANCELLED,
  DRIVER_REACHED,
  ACTIVE,
  NO_AVAILABLE_DRIVER,
  COMPLETED,
  ADMIN_CANCELLED;

  public static final Set<RideStatus> ONGOING_RIDER_STATUSES =
    Collections.unmodifiableSet(EnumSet.of(RideStatus.REQUESTED, RideStatus.DRIVER_ASSIGNED, RideStatus.DRIVER_REACHED, RideStatus.ACTIVE));

  public static final Set<RideStatus> ONGOING_DRIVER_STATUSES =
    Collections.unmodifiableSet(EnumSet.of(RideStatus.DRIVER_ASSIGNED, RideStatus.DRIVER_REACHED, RideStatus.ACTIVE));

  public static final Set<RideStatus> TERMINAL_STATUSES =
    Collections.unmodifiableSet(EnumSet.of(RIDER_CANCELLED, DRIVER_CANCELLED, ADMIN_CANCELLED, NO_AVAILABLE_DRIVER, COMPLETED, REQUEST_QUEUE_EXPIRED));
}
