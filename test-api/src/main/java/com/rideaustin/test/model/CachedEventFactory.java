package com.rideaustin.test.model;

import java.util.Date;

import org.springframework.stereotype.Component;

import com.google.maps.model.LatLng;

@Component
public class CachedEventFactory {

  private long timestamp;

  public EndRideCachedEvent createEndRideEvent(long ride, LatLng location) {
    timestamp++;
    return new EndRideCachedEvent(ride, timestamp, location.lat, location.lng);
  }

  public DriverReachCachedEvent createReachEvent(long ride) {
    timestamp++;
    return new DriverReachCachedEvent(ride, timestamp);
  }

  public void initializeTimestamp() {
    timestamp = new Date().getTime();
  }
}
