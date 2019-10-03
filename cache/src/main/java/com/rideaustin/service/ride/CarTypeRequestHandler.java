package com.rideaustin.service.ride;

import com.google.maps.model.LatLng;
import com.rideaustin.model.user.User;
import com.rideaustin.rest.exception.RideAustinException;

@FunctionalInterface
public interface CarTypeRequestHandler {

  void handleRequest(User rider, String address, LatLng location, String comment, Long cityId) throws RideAustinException;
}
