package com.rideaustin.test;

import java.util.concurrent.ThreadLocalRandom;

import com.google.maps.model.LatLng;
import com.rideaustin.model.Area;
import com.rideaustin.service.areaqueue.AreaService;

public class LocationProvider {

  private final Area airport;
  private final LatLng airportLocation;
  private final LatLng outsideAirportLocation;
  private final LatLng center = new LatLng(30.2747789,-97.7384711);

  public LocationProvider(AreaService areaService) {
    this.airport = areaService.getById(1L);
    this.airportLocation = new LatLng(airport.getAreaGeometry().getCenterPointLat(), airport.getAreaGeometry().getCenterPointLng());
    this.outsideAirportLocation = new LatLng(airport.getAreaGeometry().getTopLeftCornerLat() + 0.01, (airport.getAreaGeometry().getTopLeftCornerLng() + airport.getAreaGeometry().getBottomRightCornerLng()) / 2);
  }

  public Area getAirport() {
    return airport;
  }

  public LatLng getAirportLocation() {
    return airportLocation;
  }

  public LatLng getOutsideAirportLocation() {
    return outsideAirportLocation;
  }

  public LatLng getCenter() {
    return center;
  }

  public LatLng getRandomLocation() {
    double lat = ThreadLocalRandom.current().nextDouble(29.91209, 30.82206);
    double lng = ThreadLocalRandom.current().nextDouble(-98.05298, -97.229);
    return new LatLng(lat, lng);
  }
}
