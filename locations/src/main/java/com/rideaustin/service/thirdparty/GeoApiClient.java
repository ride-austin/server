package com.rideaustin.service.thirdparty;

import java.net.URI;
import java.util.List;

import com.google.maps.model.AddressComponent;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.rideaustin.model.Address;
import com.rideaustin.rest.exception.ServerError;

public interface GeoApiClient {

  DistanceMatrix getDrivingDistance(LatLng destination, LatLng[] origins);

  DistanceMatrix getDrivingDistanceWithDelay(LatLng origin, LatLng destination, int minutes) throws ServerError;

  GeocodingResult[] geocodeAddress(Address address) throws ServerError;

  GeocodingResult[] reverseGeocode(double lat, double lng) throws ServerError;

  URI getMapUrl(List<LatLng> points, int scale) throws ServerError;

  List<LatLng> getGoogleMiddlePoints(LatLng from, LatLng to);

  AddressComponent[] retrieveAddress(String googlePlaceId);
}
