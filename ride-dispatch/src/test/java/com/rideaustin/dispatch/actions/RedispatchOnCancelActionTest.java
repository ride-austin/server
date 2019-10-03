package com.rideaustin.dispatch.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.rideaustin.StubStateContext;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.enums.DispatchStatus;
import com.rideaustin.model.enums.EventType;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.CityDriverType;
import com.rideaustin.model.ride.CityDriverType.Configuration;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.dsl.ActiveDriverDslRepository;
import com.rideaustin.repo.dsl.RideDriverDispatchDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.model.MobileRiderRideDto;
import com.rideaustin.service.ActiveDriverLocationService;
import com.rideaustin.service.RequestedDriversRegistry;
import com.rideaustin.service.StackedDriverRegistry;
import com.rideaustin.service.event.EventsNotificationService;
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.service.model.context.RideFlowContext;
import com.rideaustin.service.model.context.RideRequestContext;
import com.rideaustin.service.notification.RideFlowPushNotificationFacade;
import com.rideaustin.service.ride.RideLoadService;
import com.rideaustin.service.user.DriverTypeCache;

public class RedispatchOnCancelActionTest {

  @Mock
  private RequestedDriversRegistry requestedDriversRegistry;
  @Mock
  private StackedDriverRegistry stackedDriverRegistry;
  @Mock
  private RideLoadService rideLoadService;
  @Mock
  private RideDslRepository rideDslRepository;
  @Mock
  private RideDriverDispatchDslRepository rideDriverDispatchDslRepository;
  @Mock
  private ActiveDriverDslRepository activeDriverDslRepository;
  @Mock
  private ActiveDriverLocationService activeDriverLocationService;
  @Mock
  private EventsNotificationService eventsNotificationService;
  @Mock
  private RideFlowPushNotificationFacade pushNotificationsFacade;
  @Mock
  private DriverTypeCache driverTypeCache;
  @Mock
  private ObjectMapper objectMapper;

  @InjectMocks
  private RedispatchOnCancelAction testedInstance;

  @Before
  public void setUp() throws Exception {
    testedInstance = new RedispatchOnCancelAction();
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void executeResetsRideState() {
    final StubStateContext context = new StubStateContext();
    final DispatchContext dispatchContext = new DispatchContext();
    final DispatchCandidate candidate = new DispatchCandidate();
    final long candidateId = 1L;
    final long rideId = 1L;
    candidate.setId(candidateId);
    dispatchContext.setCandidate(candidate);
    dispatchContext.setId(rideId);
    context.getExtendedState().getVariables().putAll(
      ImmutableMap.of(
        "requestContext", new RideRequestContext(rideId, 1L, 34.948981, -97.4891681, 1L,
          "REGULAR", 1, new ArrayList<>(), 10, null),
        "dispatchContext", dispatchContext
      )
    );
    final Ride ride = new Ride();
    ride.setId(rideId);
    ride.setCityId(1L);
    when(rideLoadService.findOneForUpdateWithRetry(eq(rideId))).thenReturn(ride);

    testedInstance.execute(context);

    assertEquals(RideStatus.REQUESTED, ride.getStatus());
    assertNull(ride.getActiveDriver());
    assertNull(ride.getDriverAcceptedOn());
    assertNotNull(ride.getRequestedOn());
    verify(rideDslRepository).save(eq(ride));
  }

  @Test
  public void executeResetsRequestedDriverType() {
    final StubStateContext context = new StubStateContext();
    final DispatchContext dispatchContext = new DispatchContext();
    final DispatchCandidate candidate = new DispatchCandidate();
    final long candidateId = 1L;
    final long rideId = 1L;
    candidate.setId(candidateId);
    dispatchContext.setCandidate(candidate);
    dispatchContext.setId(rideId);
    context.getExtendedState().getVariables().putAll(
      ImmutableMap.of(
        "requestContext", new RideRequestContext(rideId, 1L, 34.948981, -97.4891681, 1L,
          "REGULAR", 1, new ArrayList<>(), 10, null),
        "dispatchContext", dispatchContext
      )
    );
    final int requestedDriverTypeBitmask = 3;
    final Ride ride = new Ride();
    ride.setId(rideId);
    ride.setCityId(1L);
    ride.setRequestedDriverTypeBitmask(requestedDriverTypeBitmask);
    CityDriverType regular = mock(CityDriverType.class);
    CityDriverType resetting = mock(CityDriverType.class);
    when(resetting.getBitmask()).thenReturn(2);
    Configuration resetConfig = mock(Configuration.class);
    Configuration regularConfig = mock(Configuration.class);
    when(resetConfig.shouldResetOnRedispatch()).thenReturn(true);
    when(regularConfig.shouldResetOnRedispatch()).thenReturn(false);
    when(resetting.getConfigurationObject(any(ObjectMapper.class))).thenReturn(resetConfig);
    when(regular.getConfigurationObject(any(ObjectMapper.class))).thenReturn(regularConfig);
    when(rideLoadService.findOneForUpdateWithRetry(eq(rideId))).thenReturn(ride);
    when(driverTypeCache.getByCityAndBitmask(eq(rideId), eq(requestedDriverTypeBitmask))).thenReturn(ImmutableSet.of(regular, resetting));

    testedInstance.execute(context);

    assertEquals(1, ride.getRequestedDriverTypeBitmask().intValue());
  }

  @Test
  public void executeMarksDispatchRequestAsCancelled() {
    final StubStateContext context = new StubStateContext();
    final DispatchContext dispatchContext = new DispatchContext();
    final DispatchCandidate candidate = new DispatchCandidate();
    final long candidateId = 1L;
    final long rideId = 1L;
    candidate.setId(candidateId);
    dispatchContext.setCandidate(candidate);
    dispatchContext.setId(rideId);
    context.getExtendedState().getVariables().putAll(
      ImmutableMap.of(
        "requestContext", new RideRequestContext(rideId, 1L, 34.948981, -97.4891681, 1L,
          "REGULAR", 1, new ArrayList<>(), 10, null),
        "dispatchContext", dispatchContext
      )
    );
    final Ride ride = new Ride();
    ride.setId(rideId);
    ride.setCityId(1L);
    when(rideLoadService.findOneForUpdateWithRetry(eq(rideId))).thenReturn(ride);

    testedInstance.execute(context);

    verify(rideDriverDispatchDslRepository).declineRequest(eq(rideId), eq(1L), eq(DispatchStatus.CANCELLED));
  }

  @Test
  public void executePushesNotificationForNonDirectConnectRide() {
    final StubStateContext context = new StubStateContext();
    final DispatchContext dispatchContext = new DispatchContext();
    final DispatchCandidate candidate = new DispatchCandidate();
    final long candidateId = 1L;
    final long rideId = 1L;
    candidate.setId(candidateId);
    dispatchContext.setCandidate(candidate);
    dispatchContext.setId(rideId);
    final RideRequestContext requestContext = new RideRequestContext(rideId, 1L, 34.948981, -97.4891681, 1L,
      "REGULAR", 1, new ArrayList<>(), 10, null);
    context.getExtendedState().getVariables().putAll(
      ImmutableMap.of(
        "requestContext", requestContext,
        "dispatchContext", dispatchContext
      )
    );
    final Ride ride = new Ride();
    ride.setId(rideId);
    ride.setCityId(1L);
    when(rideLoadService.findOneForUpdateWithRetry(eq(rideId))).thenReturn(ride);

    testedInstance.execute(context);

    verify(pushNotificationsFacade).pushRideRedispatchNotification(eq(rideId));
  }

  @Test
  public void executeSendsDriverNotification() {
    final StubStateContext context = new StubStateContext();
    final DispatchContext dispatchContext = new DispatchContext();
    final DispatchCandidate candidate = new DispatchCandidate();
    final long candidateId = 1L;
    final long rideId = 1L;
    candidate.setId(candidateId);
    dispatchContext.setCandidate(candidate);
    dispatchContext.setId(rideId);
    final RideRequestContext requestContext = new RideRequestContext(rideId, 1L, 34.948981, -97.4891681, 1L,
      "REGULAR", 1, new ArrayList<>(), 10, null);
    context.getExtendedState().getVariables().putAll(
      ImmutableMap.of(
        "requestContext", requestContext,
        "dispatchContext", dispatchContext
      )
    );
    final Ride ride = new Ride();
    ride.setId(rideId);
    ride.setCityId(1L);
    when(rideLoadService.findOneForUpdateWithRetry(eq(rideId))).thenReturn(ride);

    testedInstance.execute(context);

    verify(eventsNotificationService).sendRideUpdateToDriver(eq(ride), any(DispatchCandidate.class), eq(EventType.DRIVER_CANCELLED));
  }

  @Test
  public void executeMarksNonStackedDriverAsAvailable() {
    final StubStateContext context = new StubStateContext();
    final DispatchContext dispatchContext = new DispatchContext();
    final DispatchCandidate candidate = new DispatchCandidate();
    final long candidateId = 1L;
    final long rideId = 1L;
    candidate.setId(candidateId);
    dispatchContext.setCandidate(candidate);
    dispatchContext.setId(rideId);
    final RideRequestContext requestContext = new RideRequestContext(rideId, 1L, 34.948981, -97.4891681, 1L,
      "REGULAR", 1, new ArrayList<>(), 10, null);
    context.getExtendedState().getVariables().putAll(
      ImmutableMap.of(
        "requestContext", requestContext,
        "dispatchContext", dispatchContext
      )
    );
    final Ride ride = new Ride();
    ride.setId(rideId);
    ride.setCityId(1L);
    when(rideLoadService.findOneForUpdateWithRetry(eq(rideId))).thenReturn(ride);

    testedInstance.execute(context);

    verify(activeDriverLocationService).updateActiveDriverLocationStatus(eq(candidateId), eq(ActiveDriverStatus.AVAILABLE));
    verify(activeDriverDslRepository).setRidingDriverAsAvailable(eq(candidateId));
  }

  @Test
  public void executeSetsStackedDriverAsStackable() {
    final StubStateContext context = new StubStateContext();
    final DispatchContext dispatchContext = new DispatchContext();
    final DispatchCandidate candidate = new DispatchCandidate();
    final long candidateId = 1L;
    final long rideId = 1L;
    candidate.setId(candidateId);
    dispatchContext.setCandidate(candidate);
    dispatchContext.setId(rideId);
    final RideRequestContext requestContext = new RideRequestContext(rideId, 1L, 34.948981, -97.4891681, 1L,
      "REGULAR", 1, new ArrayList<>(), 10, null);
    context.getExtendedState().getVariables().putAll(
      ImmutableMap.of(
        "requestContext", requestContext,
        "dispatchContext", dispatchContext
      )
    );
    final Ride ride = new Ride();
    ride.setId(rideId);
    ride.setCityId(1L);
    when(rideLoadService.findOneForUpdateWithRetry(eq(rideId))).thenReturn(ride);
    when(rideDslRepository.findPrecedingRide(eq(candidateId))).thenReturn(new MobileRiderRideDto.PrecedingRide(2L, RideStatus.ACTIVE, "", "", 34.06181, -97.9841919));

    testedInstance.execute(context);

    verify(activeDriverLocationService, never()).updateActiveDriverLocationStatus(eq(candidateId), eq(ActiveDriverStatus.AVAILABLE));
    verify(activeDriverDslRepository, never()).setRidingDriverAsAvailable(eq(candidateId));
    verify(stackedDriverRegistry).makeStackable(eq(candidateId));
  }

  @Test
  public void executeResetsRequestContext() {
    final StubStateContext context = new StubStateContext();
    final DispatchContext dispatchContext = new DispatchContext();
    final DispatchCandidate candidate = new DispatchCandidate();
    final long candidateId = 1L;
    final long rideId = 1L;
    candidate.setId(candidateId);
    dispatchContext.setCandidate(candidate);
    dispatchContext.setId(rideId);
    final RideRequestContext requestContext = new RideRequestContext(rideId, 1L, 34.948981, -97.4891681, 1L,
      "REGULAR", 1, new ArrayList<>(), 10, null);
    requestContext.setDirectConnectId("123");
    context.getExtendedState().getVariables().putAll(
      ImmutableMap.of(
        "requestContext", requestContext,
        "dispatchContext", dispatchContext
      )
    );
    final Ride ride = new Ride();
    ride.setId(rideId);
    ride.setCityId(1L);
    when(rideLoadService.findOneForUpdateWithRetry(eq(rideId))).thenReturn(ride);

    testedInstance.execute(context);

    assertNull(requestContext.getDirectConnectId());
    assertNull(requestContext.getRequestedDriverTypeBitmask());
    assertNotNull(requestContext.getCreatedDate());

  }

  @Test
  public void executeResetsFlowContext() {
    final StubStateContext context = new StubStateContext();
    final DispatchContext dispatchContext = new DispatchContext();
    final DispatchCandidate candidate = new DispatchCandidate();
    final long candidateId = 1L;
    final long rideId = 1L;
    candidate.setId(candidateId);
    dispatchContext.setCandidate(candidate);
    dispatchContext.setId(rideId);
    final RideRequestContext requestContext = new RideRequestContext(rideId, 1L, 34.948981, -97.4891681, 1L,
      "REGULAR", 1, new ArrayList<>(), 10, null);
    final RideFlowContext flowContext = new RideFlowContext();
    flowContext.setAcceptedOn(new Date());
    context.getExtendedState().getVariables().putAll(
      ImmutableMap.of(
        "requestContext", requestContext,
        "dispatchContext", dispatchContext,
        "flowContext", flowContext
      )
    );
    final Ride ride = new Ride();
    ride.setId(rideId);
    ride.setCityId(1L);
    when(rideLoadService.findOneForUpdateWithRetry(eq(rideId))).thenReturn(ride);

    testedInstance.execute(context);

    assertNull(flowContext.getAcceptedOn());
  }

}