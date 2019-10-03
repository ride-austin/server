package com.rideaustin.test.model;

import java.util.Collections;
import java.util.Map;

public class DriverReachCachedEvent extends CachedEvent {
  DriverReachCachedEvent(long rideId, long timestamp) {
    super(Type.DRIVER_REACHED, rideId, timestamp);
  }

  @Override
  public Map<String, String> getProperties() {
    return Collections.emptyMap();
  }
}
