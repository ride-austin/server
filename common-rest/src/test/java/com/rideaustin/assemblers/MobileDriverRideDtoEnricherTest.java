package com.rideaustin.assemblers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.statemachine.StateMachinePersist;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.StubStateMachineContext;
import com.rideaustin.model.Address;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.RideUpgradeRequest;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.model.MobileDriverRideDto;
import com.rideaustin.rest.model.MobileDriverRideDto.RequestedDispatchType;
import com.rideaustin.service.StackedDriverRegistry;
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.service.model.context.RideRequestContext;
import com.rideaustin.service.ride.RideUpgradeService;
import com.rideaustin.service.user.DriverTypeCache;
import com.rideaustin.service.user.DriverTypeUtils;

public class MobileDriverRideDtoEnricherTest {

  @Mock
  private RideUpgradeService upgradeService;
  @Mock
  private StateMachinePersist<States, Events, String> contextAccess;
  @Mock
  private Environment environment;
  @Mock
  private StackedDriverRegistry stackedDriverRegistry;
  @Mock
  private RideDslRepository rideDslRepository;
  @Mock
  private DriverTypeCache driverTypeCache;

  private MobileDriverRideDtoEnricher testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    DriverTypeUtils.setDriverTypeCache(driverTypeCache);
    testedInstance = new MobileDriverRideDtoEnricher(upgradeService, contextAccess, environment, stackedDriverRegistry, rideDslRepository);
  }

  @Test
  public void enrichSkipsNull() {
    final MobileDriverRideDto result = testedInstance.enrich(null);

    assertNull(result);
  }

  @Test
  public void enrichSetsUpgradeRequest() {
    MobileDriverRideDto source = new MobileDriverRideDto(1L, 1L, RideStatus.DRIVER_ASSIGNED, 1L, "url", "A",
      "B", "+15555555555", "email@email.com", 5.0, 34.18961,
      -97.168161, null, null, "C", null, new Address(), null,
      BigDecimal.ONE, null, "D", "REGULAR", "url", "{}",
      null, null, null, null);

    when(upgradeService.getRequest(eq(source.getId()), eq(source.getDriverId()))).thenReturn(Optional.of(new RideUpgradeRequest()));

    final MobileDriverRideDto result = testedInstance.enrich(source);

    assertNotNull(result.getUpgradeRequest());
  }

  @Test
  public void enrichSetsETA() throws Exception {
    MobileDriverRideDto source = new MobileDriverRideDto(1L, 1L, RideStatus.DRIVER_ASSIGNED, 1L, "url", "A",
      "B", "+15555555555", "email@email.com", 5.0, 34.18961,
      -97.168161, null, null, "C", null, new Address(), null,
      BigDecimal.ONE, null, "D", "REGULAR", "url", "{}",
      null, null, null, null);

    final DispatchContext dispatchContext = new DispatchContext();
    final DispatchCandidate candidate = new DispatchCandidate();
    final long eta = 100L;
    candidate.setDrivingTimeToRider(eta);
    dispatchContext.setCandidate(candidate);
    when(upgradeService.getRequest(eq(source.getId()), eq(source.getDriverId()))).thenReturn(Optional.empty());
    when(contextAccess.read(anyString())).thenReturn(new StubStateMachineContext(ImmutableMap.of("dispatchContext", dispatchContext)));

    final MobileDriverRideDto result = testedInstance.enrich(source);

    assertEquals(eta, result.getEstimatedTimeArrive().longValue());
  }

  @Test
  public void enrichSetsStackedRide() throws Exception {
    MobileDriverRideDto source = new MobileDriverRideDto(1L, 1L, RideStatus.ACTIVE, 1L, "url", "A",
      "B", "+15555555555", "email@email.com", 5.0, 34.18961,
      -97.168161, null, null, "C", null, new Address(), null,
      BigDecimal.ONE, null, "D", "REGULAR", "url", "{}",
      null, null, null, null);

    MobileDriverRideDto nextRide = new MobileDriverRideDto(1L, 1L, RideStatus.DRIVER_ASSIGNED, 1L, "url", "A",
      "B", "+15555555555", "email@email.com", 5.0, 34.18961,
      -97.168161, null, null, "C", null, new Address(), null,
      BigDecimal.ONE, null, "D", "REGULAR", "url", "{}",
      null, null, null, null);

    final DispatchContext dispatchContext = new DispatchContext();
    final DispatchCandidate candidate = new DispatchCandidate();
    final long eta = 100L;
    candidate.setId(1L);
    candidate.setDrivingTimeToRider(eta);
    dispatchContext.setCandidate(candidate);
    when(upgradeService.getRequest(eq(source.getId()), eq(source.getDriverId()))).thenReturn(Optional.empty());
    when(contextAccess.read(anyString())).thenReturn(new StubStateMachineContext(ImmutableMap.of("dispatchContext", dispatchContext)));
    when(stackedDriverRegistry.isStacked(eq(candidate.getId()))).thenReturn(true);
    when(rideDslRepository.findNextRide(candidate.getId())).thenReturn(nextRide);

    final MobileDriverRideDto result = testedInstance.enrich(source);

    assertNotNull(result.getNextRide());
  }

  @Test
  public void enrichSetsRegularDispatchType() throws Exception {
    MobileDriverRideDto source = setupRequestedDispatchType(null);

    final MobileDriverRideDto result = testedInstance.enrich(source);

    assertEquals(RequestedDispatchType.REGULAR, result.getRequestedDispatchType());
  }

  @Test
  public void enrichSetsDirectConnectDispatchType() throws Exception {
    MobileDriverRideDto source = setupRequestedDispatchType("16565");

    final MobileDriverRideDto result = testedInstance.enrich(source);

    assertEquals(RequestedDispatchType.DIRECT_CONNECT, result.getRequestedDispatchType());
  }

  private MobileDriverRideDto setupRequestedDispatchType(final String directConnectId) throws Exception {
    MobileDriverRideDto source = new MobileDriverRideDto(1L, 1L, RideStatus.DRIVER_ASSIGNED, 1L, "url", "A",
      "B", "+15555555555", "email@email.com", 5.0, 34.18961,
      -97.168161, null, null, "C", null, new Address(), null,
      BigDecimal.ONE, null, "D", "REGULAR", "url", "{}",
      null, null, null, null);

    final DispatchContext dispatchContext = new DispatchContext();
    final DispatchCandidate candidate = new DispatchCandidate();
    final RideRequestContext rideRequestContext = new RideRequestContext();
    final long eta = 100L;
    candidate.setDrivingTimeToRider(eta);
    dispatchContext.setCandidate(candidate);
    rideRequestContext.setDirectConnectId(directConnectId);
    when(upgradeService.getRequest(eq(source.getId()), eq(source.getDriverId()))).thenReturn(Optional.empty());
    when(contextAccess.read(anyString())).thenReturn(new StubStateMachineContext(ImmutableMap.of(
      "dispatchContext", dispatchContext,
      "requestContext", rideRequestContext
    )));
    return source;
  }
}