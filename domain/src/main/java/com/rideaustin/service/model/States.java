package com.rideaustin.service.model;

import java.util.EnumSet;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.rideaustin.model.enums.RideStatus;

public enum States {
  REQUESTED {
    @Override
    public boolean isBefore(States state) {
      return true;
    }
  },
  HANDSHAKE_PENDING {
    @Override
    public boolean isBefore(States state) {
      return true;
    }
  },
  DISPATCH_PENDING {
    @Override
    public boolean isBefore(States state) {
      return true;
    }
  },
  DRIVER_ASSIGNED {
    @Override
    public boolean isBefore(States state) {
      return !DISPATCH_PENDING.equals(state);
    }
  },
  DRIVER_REACHED {
    @Override
    public boolean isBefore(States state) {
      return !EnumSet.of(REQUESTED, DISPATCH_PENDING, DRIVER_ASSIGNED).contains(state);
    }
  },
  ACTIVE {
    @Override
    public boolean isBefore(States state) {
      return !EnumSet.of(REQUESTED, DISPATCH_PENDING, DRIVER_ASSIGNED, DRIVER_REACHED).contains(state);
    }
  },
  COMPLETED {
    @Override
    public boolean isBefore(States state) {
      return ENDED.equals(state);
    }
  },
  DRIVER_CANCELLED {
    @Override
    public boolean isBefore(States state) {
      return ENDED.equals(state);
    }
  },
  RIDER_CANCELLED {
    @Override
    public boolean isBefore(States state) {
      return ENDED.equals(state);
    }
  },
  ADMIN_CANCELLED {
    @Override
    public boolean isBefore(States state) {
      return ENDED.equals(state);
    }
  },
  NO_AVAILABLE_DRIVER {
    @Override
    public boolean isBefore(States state) {
      return ENDED.equals(state);
    }
  },
  ENDED {
    @Override
    public boolean isBefore(States state) {
      return false;
    }
  };

  public static final BiMap<RideStatus, States> MAPPING = ImmutableBiMap.<RideStatus, States>builder()
    .put(RideStatus.REQUESTED, REQUESTED)
    .put(RideStatus.DRIVER_ASSIGNED, DRIVER_ASSIGNED)
    .put(RideStatus.DRIVER_REACHED, DRIVER_REACHED)
    .put(RideStatus.ACTIVE, ACTIVE)
    .put(RideStatus.COMPLETED, COMPLETED)
    .put(RideStatus.DRIVER_CANCELLED, DRIVER_CANCELLED)
    .put(RideStatus.RIDER_CANCELLED, RIDER_CANCELLED)
    .put(RideStatus.ADMIN_CANCELLED, ADMIN_CANCELLED)
    .put(RideStatus.NO_AVAILABLE_DRIVER, NO_AVAILABLE_DRIVER)
    .build();

  public abstract boolean isBefore(States state);
}
