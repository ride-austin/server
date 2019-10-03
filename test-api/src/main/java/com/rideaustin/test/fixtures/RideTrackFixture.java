package com.rideaustin.test.fixtures;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;

import com.google.maps.model.LatLng;
import com.rideaustin.model.ride.RideTracker;

public class RideTrackFixture extends AbstractFixture<List<RideTracker>> {

  private final List<RideTracker> trackers = new ArrayList<>();

  @Override
  protected List<RideTracker> createObject() {
    return trackers;
  }

  public RideTrackFixture add(LatLng location, long sequence, Date startTrack, int deltaSeconds) {
    trackers.add(
      RideTracker.builder()
        .sequence(sequence)
        .latitude(location.lat)
        .longitude(location.lng)
        .trackedOn(DateUtils.addSeconds(startTrack, deltaSeconds))
        .build()
    );
    return this;
  }

  public void setRideId(long rideId) {
    for (RideTracker tracker : trackers) {
      tracker.setRideId(rideId);
    }
  }
}
