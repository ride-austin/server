package com.rideaustin.service.ride;

import java.util.Map;

import com.rideaustin.service.ride.events.CachedEventType;

import lombok.Getter;

@Getter
public abstract class RideEvent {

  private final Long rideId;
  private final Long timestamp;
  private final CachedEventType eventType;

  public RideEvent(Map<String, String> eventProperties) {
    rideId = Long.parseLong(eventProperties.get("rideId"));
    timestamp = Long.parseLong(eventProperties.get("eventTimestamp"));
    eventType = CachedEventType.valueOf(eventProperties.get("eventType"));
  }

  @Override
  public String toString() {
    return String.format("%s{rideId=%d, timestamp=%d, eventType='%s'}", this.getClass().getSimpleName(), rideId, timestamp, eventType);
  }
}
