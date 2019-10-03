package com.rideaustin.rest.model;

import java.util.Optional;

import com.google.maps.model.LatLng;

public interface RideLocation {

  Double getLat();
  Double getLng();
  String getAddress();
  String getZipCode();
  String getGooglePlaceId();

  default Optional<LatLng> asLatLng() {
    if (getLat() != null && getLng() != null) {
      return Optional.of(new LatLng(getLat(), getLng()));
    }
    return Optional.empty();
  }

}
