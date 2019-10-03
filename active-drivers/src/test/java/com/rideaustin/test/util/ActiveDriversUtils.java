package com.rideaustin.test.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.rideaustin.model.ride.RideTracker;

public class ActiveDriversUtils {

  public static List<RideTracker> mockRideTrackers() {
    return mockRideTrackers(4);
  }

  public static List<RideTracker> mockRideTrackers(int i) {
    long time = new Date().getTime();
    List<RideTracker> result = new ArrayList<>();
    for (int j = 0; j < i; j++) {
      result.add(mockRideTracker((long)j * 1000, 12.0, 13.0, 14.0, 15.0, 16.0, time+=1000));
    }
    return result;
  }

  public static RideTracker mockRideTracker(Long sequence, Double lat, Double lng, Double speed, Double heading, Double course, long time) {
    RideTracker rideTracker = new RideTracker();
    rideTracker.setSequence(sequence);
    rideTracker.setLatitude(lat);
    rideTracker.setLongitude(lng);
    rideTracker.setSpeed(speed);
    rideTracker.setHeading(heading);
    rideTracker.setCourse(course);
    rideTracker.setTrackedOn(new Date(time));
    return rideTracker;
  }
}
