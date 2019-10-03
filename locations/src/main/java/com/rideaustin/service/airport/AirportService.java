package com.rideaustin.service.airport;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import com.google.maps.model.LatLng;
import com.rideaustin.model.Area;
import com.rideaustin.model.City;
import com.rideaustin.model.airports.Airport;
import com.rideaustin.model.surgepricing.AreaGeometry;
import com.rideaustin.rest.model.Location;
import com.rideaustin.service.CityService;
import com.rideaustin.utils.GeometryUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AirportService {

  private final AirportCache airportCache;
  private final CityService cityService;

  public Optional<Airport> getAirportForLocation(Double lat, Double lng) {
    if (lat == null || lng == null) {
      return Optional.empty();
    }
    return getAirportForLocation(new LatLng(lat, lng));
  }

  public Optional<Airport> getAirportForLocation(LatLng location) {
    final City closestCity = cityService.findClosestByCoordinates(new Location(location.lat, location.lng));
    List<Airport> airports = airportCache.getAirports().values().stream()
      .filter(a -> Objects.equals(closestCity.getId(), a.getCityId()) && isInsideAirportArea(a, location))
      .collect(Collectors.toList());
    if (CollectionUtils.isEmpty(airports)) {
      return Optional.empty();
    } else if (airports.size() > 1) {
      throw new IllegalStateException("Wrong airport config for fee. Multiple airports for single location");
    } else {
      return Optional.of(airports.get(0));
    }
  }

  public boolean isInsideAirportArea(Airport airport, LatLng location) {
    final Optional<AreaGeometry> areaGeometry = Optional.ofNullable(airport.getArea())
      .map(Area::getAreaGeometry);
    if (areaGeometry.isPresent()) {
      GeometryUtils.updatePolygon(areaGeometry.get());
      return GeometryUtils.isInsideArea(airport.getArea(), location);
    } else {
      return false;
    }
  }

}
