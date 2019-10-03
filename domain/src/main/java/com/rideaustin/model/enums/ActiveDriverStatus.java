package com.rideaustin.model.enums;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum ActiveDriverStatus {
  AVAILABLE,
  REQUESTED,
  RIDING,
  INACTIVE,
  AWAY;

  public static final Set<ActiveDriverStatus> ONGOING_ACTIVE_DRIVER_STATUSES = Collections.unmodifiableSet(EnumSet.of(REQUESTED, RIDING));
}
