package com.rideaustin.service;

import static com.rideaustin.service.FareTestConstants.money;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

import org.apache.commons.lang3.RandomUtils;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.joda.time.DateTime;

import com.google.maps.model.LatLng;
import com.rideaustin.model.airports.Airport;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.ride.FareDetails;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Rider;
import com.rideaustin.service.airport.AirportService;

public class FareServiceTestUtils {

  static final double AIRPORT_FEE = 3.0;

  static Optional<Airport> stubAirport() {
    Airport airport = new Airport();
    airport.setId(RandomUtils.nextLong(1, 10));
    airport.setPickupFee(Money.of(CurrencyUnit.USD, AIRPORT_FEE));
    airport.setDropoffFee(Money.of(CurrencyUnit.USD, AIRPORT_FEE));

    return Optional.of(airport);
  }

  static void setupAirport(final AirportService airportService) {
    setupAirport(false, null, airportService);
  }

  static void setupAirport(boolean inAirport, LatLng location, final AirportService airportService) {
    Optional<Airport> airport = inAirport ? stubAirport() : Optional.empty();
    if (location == null) {
      when(airportService.getAirportForLocation(any())).thenReturn(airport);
      when(airportService.getAirportForLocation(anyDouble(), anyDouble())).thenReturn(airport);
    } else {
      when(airportService.getAirportForLocation(location)).thenReturn(airport);
      when(airportService.getAirportForLocation(location.lat, location.lng)).thenReturn(airport);
    }
  }

  static CarType stubCarType() {
    return new CarType();
  }

  static void prepareRide(final Ride ride, final LatLng pickupLatLng, final LatLng dropoffLatLng) {
    ride.setRequestedCarType(FareServiceTestUtils.stubCarType());
    ride.setRider(new Rider());
    ride.setStartLocationLat(pickupLatLng.lat);
    ride.setStartLocationLong(pickupLatLng.lng);
    ride.setEndLocationLat(dropoffLatLng.lat);
    ride.setEndLocationLong(dropoffLatLng.lng);
    ride.setDistanceTravelled(new BigDecimal(1000));
    ride.setStartedOn(new DateTime().minusHours(5).toDate());
    ride.setCompletedOn(new Date());
    ride.setFareDetails(FareDetails.builder()
      .distanceFare(money(1.0))
      .totalFare(money(10d)).build());
  }
}
