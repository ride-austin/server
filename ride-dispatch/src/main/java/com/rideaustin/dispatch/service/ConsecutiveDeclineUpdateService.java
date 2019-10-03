package com.rideaustin.dispatch.service;

import com.rideaustin.model.ride.Ride;
import com.rideaustin.service.model.DispatchRequest;

@FunctionalInterface
public interface ConsecutiveDeclineUpdateService {
  void processDriverDecline(Ride ride, DispatchRequest request);
}
