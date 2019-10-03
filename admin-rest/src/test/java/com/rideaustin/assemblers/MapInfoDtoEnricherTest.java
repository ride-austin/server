package com.rideaustin.assemblers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.statemachine.StateMachinePersist;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.rideaustin.StubStateMachineContext;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.rest.model.MapInfoDto;
import com.rideaustin.service.RequestedDriversRegistry;
import com.rideaustin.service.location.model.LocationObject;
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.DispatchContext;

public class MapInfoDtoEnricherTest {

  @Mock
  private RequestedDriversRegistry requestedDriversRegistry;
  @Mock
  private StateMachinePersist<States, Events, String> contextAccess;
  @Mock
  private Environment environment;

  private MapInfoDtoEnricher testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new MapInfoDtoEnricher(requestedDriversRegistry, contextAccess, environment);
  }

  @Test
  public void enrichSetsActiveDriverFromRideObject() {
    final MapInfoDto ride = new MapInfoDto(1L, 1L, 34.6816881, -97.686816,
      RideStatus.DRIVER_ASSIGNED, 1L);
    final OnlineDriverDto driverDto = new OnlineDriverDto();
    final LocationObject locationObject = new LocationObject();
    locationObject.setLatitude(34.1681681);
    locationObject.setLongitude(-97.4681381);
    driverDto.setLocationObject(locationObject);
    driverDto.setStatus(ActiveDriverStatus.RIDING);

    final List<MapInfoDto> result = testedInstance.enrich(new ArrayList<>(ImmutableList.of(ride)),
      new ArrayList<>(ImmutableList.of(driverDto)));

    assertEquals(1, result.size());
    final MapInfoDto enriched = result.get(0);
    assertEquals(1L, enriched.getActiveDriverId().longValue());
    assertEquals(1L, enriched.getId());
    assertEquals(RideStatus.DRIVER_ASSIGNED, enriched.getStatus());
  }

  @Test
  public void enrichSetsActiveDriverFromPersistedContext() throws Exception {
    final MapInfoDto ride = new MapInfoDto(1L, null, 34.6816881, -97.686816,
      RideStatus.DRIVER_ASSIGNED, 1L);
    final OnlineDriverDto driverDto = new OnlineDriverDto();
    final LocationObject locationObject = new LocationObject();
    locationObject.setLatitude(34.1681681);
    locationObject.setLongitude(-97.4681381);
    driverDto.setLocationObject(locationObject);
    driverDto.setStatus(ActiveDriverStatus.RIDING);
    final DispatchContext dispatchContext = new DispatchContext();
    final DispatchCandidate candidate = new DispatchCandidate();
    candidate.setId(1L);
    dispatchContext.setCandidate(candidate);
    when(contextAccess.read(anyString())).thenReturn(new StubStateMachineContext(ImmutableMap.of(
      "dispatchContext", dispatchContext
    )));

    final List<MapInfoDto> result = testedInstance.enrich(new ArrayList<>(ImmutableList.of(ride)),
      new ArrayList<>(ImmutableList.of(driverDto)));

    assertEquals(1, result.size());
    final MapInfoDto enriched = result.get(0);
    assertEquals(1L, enriched.getActiveDriver().getDriver().getId());
    assertEquals(1L, enriched.getId());
    assertEquals(RideStatus.DRIVER_ASSIGNED, enriched.getStatus());
  }


}