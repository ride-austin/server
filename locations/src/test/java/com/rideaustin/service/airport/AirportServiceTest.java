package com.rideaustin.service.airport;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.maps.model.LatLng;
import com.rideaustin.model.Area;
import com.rideaustin.model.City;
import com.rideaustin.model.airports.Airport;
import com.rideaustin.model.surgepricing.AreaGeometry;
import com.rideaustin.service.CityService;

@RunWith(MockitoJUnitRunner.class)
public class AirportServiceTest {

  @Mock
  private AirportCache airportCache;
  @Mock
  private CityService cityService;

  private AirportService airportService;

  private Airport airportAustin;
  private LatLng locationAustin;

  @Before
  public void setUp() throws Exception {
    airportService = new AirportService(airportCache, cityService);

    airportAustin = new Airport();
    airportAustin.setId(1L);
    airportAustin.setName("Austin Bergstrom");
    airportAustin.setCityId(1L);
    Area area = new Area();
    AreaGeometry areaGeometry = new AreaGeometry();
    areaGeometry.setCsvGeometry("-97.6806460,30.2112030 -97.6806460,30.1800798 -97.6560610,30.1800540 -97.6560610,30.2112030");
    area.setAreaGeometry(areaGeometry);
    airportAustin.setArea(area);

    final City austin = new City();
    austin.setId(1L);
    when(cityService.findClosestByCoordinates(any())).thenReturn(austin);
    when(airportCache.getAirports()).thenReturn(Collections.singletonMap(airportAustin.getId(), airportAustin));

    locationAustin = new LatLng(30.204420, -97.662616); // location in Austin Bergstrom airport
  }

  @Test
  public void testGetAirportForLocation() throws Exception {

    Optional<Airport> airport = airportService.getAirportForLocation(locationAustin);

    assertTrue(airport.isPresent());
    assertEquals(airportAustin, airport.get());
  }

  @Test
  public void testNoAirportLocation() {
    LatLng location = new LatLng(30.241863, -97.778127); // somewhere in Austin, outside the airport

    assertFalse(airportService.getAirportForLocation(location).isPresent());
  }

  @Test
  public void testIsInsideAirportArea() throws Exception {
    assertTrue(airportService.isInsideAirportArea(airportAustin, locationAustin));
  }

}