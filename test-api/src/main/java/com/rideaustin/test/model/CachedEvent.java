package com.rideaustin.test.model;

import java.util.Map;

import lombok.Getter;

@Getter
public abstract class CachedEvent {

  public enum Type {
    END_RIDE,
    DRIVER_REACHED,
    START_RIDE,
    CANCEL_RIDE
  }

  private final Type type;
  private final long rideId;
  private final long timestamp;

  CachedEvent(Type type, long rideId, long timestamp) {
    this.type = type;
    this.rideId = rideId;
    this.timestamp = timestamp;
  }

  public abstract Map<String, String> getProperties();
}
