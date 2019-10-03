package com.rideaustin.test.model;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

public class EndRideCachedEvent extends CachedEvent {

  private final double endLocationLat;
  private final double endLocationLong;

  EndRideCachedEvent(long rideId, long timestamp, double endLocationLat, double endLocationLong) {
    super(Type.END_RIDE, rideId, timestamp);
    this.endLocationLat = endLocationLat;
    this.endLocationLong = endLocationLong;
  }

  @Override
  public Map<String, String> getProperties() {
    return ImmutableMap.of("endLocationLat", String.valueOf(endLocationLat),
      "endLocationLong", String.valueOf(endLocationLong));
  }
}
