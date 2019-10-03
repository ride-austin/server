package com.rideaustin.service.airport;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.model.airports.Airport;
import com.rideaustin.repo.dsl.AirportDslRepository;

public class AirportCacheTest {

  @Mock
  private AirportDslRepository airportDslRepository;

  private AirportCache testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new AirportCache(airportDslRepository);
  }

  @Test
  public void getAirportsTransformsListToMap() {
    final long airportId = 1L;
    final Airport airport = new Airport();
    airport.setId(airportId);
    when(airportDslRepository.findAll()).thenReturn(Collections.singletonList(airport));

    final Map<Long, Airport> result = testedInstance.getAirports();

    assertTrue(result.containsKey(airportId));
    assertEquals(airport, result.get(airportId));
  }
}