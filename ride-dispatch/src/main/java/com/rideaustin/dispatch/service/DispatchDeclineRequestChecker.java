package com.rideaustin.dispatch.service;

import com.rideaustin.model.ride.Ride;

public interface DispatchDeclineRequestChecker<T> {

  boolean checkIfActiveDriverNeedAddConsecutiveDeclineRequest(T activeDriverData, Ride ride);

  T createActiveDriverData(long activeDriverId);
}