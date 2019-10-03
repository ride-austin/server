package com.rideaustin.rest.model;

import java.util.List;

import com.rideaustin.model.ride.RideTracker;

public class RideTrackers {

  private List<RideTracker> trackers;

  public List<RideTracker> getTrackers() {
    return trackers;
  }

  public void setTrackers(List<RideTracker> trackers) {
    this.trackers = trackers;
  }
}
