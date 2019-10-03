package com.rideaustin.dispatch.actions;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.springframework.core.env.Environment;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.web.context.request.async.DeferredResult;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.StubStateContext;
import com.rideaustin.StubStateMachineContext;
import com.rideaustin.model.Address;
import com.rideaustin.model.Session;
import com.rideaustin.model.airports.Airport;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.ride.RideTracker;
import com.rideaustin.repo.dsl.ActiveDriverDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.repo.dsl.SessionDslRepository;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.rest.model.MobileDriverRideDto;
import com.rideaustin.rest.model.RideEndLocation;
import com.rideaustin.service.ActiveDriverLocationService;
import com.rideaustin.service.FareService;
import com.rideaustin.service.MapService;
import com.rideaustin.service.RideTrackerService;
import com.rideaustin.service.SchedulerService;
import com.rideaustin.service.StackedDriverRegistry;
import com.rideaustin.service.airport.AirportService;
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.service.model.context.RideFlowContext;
import com.rideaustin.service.model.context.RideRequestContext;
import com.rideaustin.service.ride.RideLoadService;
import com.rideaustin.service.ride.jobs.RideSummaryJob;

public class EndRideActionTest {

  @Mock
  private RideTrackerService rideTrackerService;
  @Mock
  private AirportService airportService;
  @Mock
  private MapService mapService;
  @Mock
  private RideLoadService rideLoadService;
  @Mock
  private FareService fareService;
  @Mock
  private RideDslRepository rideDslRepository;
  @Mock
  private ActiveDriverDslRepository activeDriverDslRepository;
  @Mock
  private ActiveDriverLocationService activeDriverLocationService;
  @Mock
  private SchedulerService schedulerService;
  @Mock
  private SessionDslRepository sessionDslRepository;
  @Mock
  private StackedDriverRegistry stackedDriverRegistry;
  @Mock
  private StateMachinePersist<States, Events, String> contextAccess;
  @Mock
  private DeferredResult<MobileDriverRideDto> deferredResult;
  @Mock
  private Environment environment;

  @InjectMocks
  private EndRideAction testedInstance;

  @Before
  public void setUp() throws Exception {
    testedInstance = new EndRideAction();

    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void executeFillsEndLocation() throws RideAustinException {
    final long rideId = 1L;
    final Ride ride = new Ride();
    when(rideLoadService.findOneForUpdateWithRetry(eq(rideId))).thenReturn(ride);
    final StubStateContext context = new StubStateContext();
    final DispatchContext dispatchContext = new DispatchContext();
    final DispatchCandidate candidate = new DispatchCandidate();
    candidate.setId(1L);
    dispatchContext.setCandidate(candidate);
    context.getStateMachine().getExtendedState().getVariables().putAll(
      ImmutableMap.of(
        "requestContext", new RideRequestContext(),
        "flowContext", new RideFlowContext(),
        "dispatchContext", dispatchContext,
        "rideId", rideId
      )
    );
    final RideEndLocation rideEndLocation = new RideEndLocation();
    final double endLocationLat = 34.681918;
    final double endLocationLong = -97.6465165;
    rideEndLocation.setEndLocationLat(endLocationLat);
    rideEndLocation.setEndLocationLong(endLocationLong);
    context.addMessageHeader("endLocation", rideEndLocation);
    context.addMessageHeader("completedOn", new Date());
    final Address address = new Address();
    address.setAddress("ADDRESS");
    when(mapService.reverseGeocodeAddress(anyDouble(), anyDouble())).thenReturn(address);
    when(rideTrackerService.endRide(eq(rideId), any(RideTracker.class))).thenAnswer((Answer<RideTracker>) invocation -> (RideTracker) invocation.getArguments()[1]);
    when(airportService.getAirportForLocation(anyDouble(), anyDouble())).thenReturn(Optional.empty());

    testedInstance.execute(context);

    assertEquals(endLocationLat, ride.getEndLocationLat(), 0.0);
    assertEquals(endLocationLong, ride.getEndLocationLong(), 0.0);
    assertEquals(address.getAddress(), ride.getEnd().getAddress());
  }

  @Test
  public void executeSetsCompletedOn() {
    final long rideId = 1L;
    final Date completedOn = new Date();
    final Ride ride = new Ride();
    final StubStateContext context = createContext(rideId, ride, completedOn);
    when(rideTrackerService.endRide(eq(rideId), any(RideTracker.class))).thenAnswer((Answer<RideTracker>) invocation -> (RideTracker) invocation.getArguments()[1]);
    when(airportService.getAirportForLocation(anyDouble(), anyDouble())).thenReturn(Optional.empty());

    testedInstance.execute(context);

    assertEquals(completedOn, ride.getCompletedOn());
  }

  @Test
  public void executeSetsCompletedStatus() {
    final long rideId = 1L;
    final Date completedOn = new Date();
    final Ride ride = new Ride();
    final StubStateContext context = createContext(rideId, ride, completedOn);
    when(rideTrackerService.endRide(eq(rideId), any(RideTracker.class))).thenAnswer((Answer<RideTracker>) invocation -> (RideTracker) invocation.getArguments()[1]);
    when(airportService.getAirportForLocation(anyDouble(), anyDouble())).thenReturn(Optional.empty());

    testedInstance.execute(context);

    assertEquals(RideStatus.COMPLETED, ride.getStatus());
  }

  @Test
  public void executeSetsFlowContextData() {
    final long rideId = 1L;
    final Date completedOn = new Date();
    final Ride ride = new Ride();
    final StubStateContext context = createContext(rideId, ride, completedOn);
    final RideFlowContext flowContext = context.getExtendedState().get("flowContext", RideFlowContext.class);
    final Date startedOn = new Date();
    final Date acceptedOn = new Date();
    final Date reachedOn = new Date();
    final Session session = new Session();
    flowContext.setStartedOn(startedOn);
    flowContext.setAcceptedOn(acceptedOn);
    flowContext.setReachedOn(reachedOn);
    flowContext.setDriverSession(1L);
    when(rideTrackerService.endRide(eq(rideId), any(RideTracker.class))).thenAnswer((Answer<RideTracker>) invocation -> (RideTracker) invocation.getArguments()[1]);
    when(airportService.getAirportForLocation(anyDouble(), anyDouble())).thenReturn(Optional.empty());
    when(sessionDslRepository.findOne(eq(flowContext.getDriverSession()))).thenReturn(session);

    testedInstance.execute(context);

    assertEquals(startedOn, ride.getStartedOn());
    assertEquals(acceptedOn, ride.getDriverAcceptedOn());
    assertEquals(reachedOn, ride.getDriverReachedOn());
    assertEquals(session, ride.getDriverSession());
  }

  @Test
  public void executeSetsDistanceTravelled() {
    final long rideId = 1L;
    final Date completedOn = new Date();
    final Ride ride = new Ride();
    final StubStateContext context = createContext(rideId, ride, completedOn);
    final BigDecimal distanceTravelled = BigDecimal.valueOf(1000L);
    when(rideTrackerService.endRide(eq(rideId), any(RideTracker.class))).thenAnswer((Answer<RideTracker>) invocation -> {
      final RideTracker rideTracker = (RideTracker) invocation.getArguments()[1];
      rideTracker.setDistanceTravelled(distanceTravelled);
      return rideTracker;
    });
    when(airportService.getAirportForLocation(anyDouble(), anyDouble())).thenReturn(Optional.empty());

    testedInstance.execute(context);

    assertEquals(distanceTravelled, ride.getDistanceTravelled());
  }

  @Test
  public void executeSetsAirport() {
    final long rideId = 1L;
    final Date completedOn = new Date();
    final Ride ride = new Ride();
    final Airport airport = new Airport();
    airport.setId(1L);
    final StubStateContext context = createContext(rideId, ride, completedOn);
    when(rideTrackerService.endRide(eq(rideId), any(RideTracker.class))).thenAnswer((Answer<RideTracker>) invocation -> (RideTracker) invocation.getArguments()[1]);
    when(airportService.getAirportForLocation(anyDouble(), anyDouble())).thenReturn(Optional.of(airport));

    testedInstance.execute(context);

    assertEquals(airport.getId(), ride.getAirportId().longValue());
  }

  @Test
  public void executeCallsFareCalculation() {
    final long rideId = 1L;
    final Date completedOn = new Date();
    final Ride ride = new Ride();
    final StubStateContext context = createContext(rideId, ride, completedOn);
    when(rideTrackerService.endRide(eq(rideId), any(RideTracker.class))).thenAnswer((Answer<RideTracker>) invocation -> (RideTracker) invocation.getArguments()[1]);
    when(airportService.getAirportForLocation(anyDouble(), anyDouble())).thenReturn(Optional.empty());

    testedInstance.execute(context);

    verify(fareService, times(1)).calculateTotals(eq(ride));
  }

  @Test
  public void executeSetsDeferredResult() {
    final long rideId = 1L;
    final Date completedOn = new Date();
    final Ride ride = new Ride();
    final StubStateContext context = createContext(rideId, ride, completedOn);
    MobileDriverRideDto driverRideDto = mock(MobileDriverRideDto.class);
    when(rideTrackerService.endRide(eq(rideId), any(RideTracker.class))).thenAnswer((Answer<RideTracker>) invocation -> (RideTracker) invocation.getArguments()[1]);
    when(airportService.getAirportForLocation(anyDouble(), anyDouble())).thenReturn(Optional.empty());
    when(rideDslRepository.findOneForDriver(eq(rideId))).thenReturn(driverRideDto);

    testedInstance.execute(context);

    verify(deferredResult, times(1)).setResult(eq(driverRideDto));
  }

  @Test
  public void executeSetsDriverAvailable() {
    final long rideId = 1L;
    final Date completedOn = new Date();
    final Ride ride = new Ride();
    final StubStateContext context = createContext(rideId, ride, completedOn);
    final DispatchContext dispatchContext = context.getExtendedState().get("dispatchContext", DispatchContext.class);
    when(rideTrackerService.endRide(eq(rideId), any(RideTracker.class))).thenAnswer((Answer<RideTracker>) invocation -> (RideTracker) invocation.getArguments()[1]);
    when(airportService.getAirportForLocation(anyDouble(), anyDouble())).thenReturn(Optional.empty());

    testedInstance.execute(context);

    verify(activeDriverLocationService, times(1)).updateActiveDriverLocationStatus(eq(dispatchContext.getCandidate().getId()), eq(ActiveDriverStatus.AVAILABLE));
    verify(activeDriverDslRepository, times(1)).setRidingDriverAsAvailable(eq(dispatchContext.getCandidate().getId()));
  }

  @Test
  public void executeUpdatesNextStackedRide() throws Exception {
    final long rideId = 1L;
    final Date completedOn = new Date();
    final Ride ride = new Ride();
    final MobileDriverRideDto nextRide = mock(MobileDriverRideDto.class);
    final StubStateContext context = createContext(rideId, ride, completedOn);
    final DispatchContext dispatchContext = context.getExtendedState().get("dispatchContext", DispatchContext.class);
    final RideFlowContext nextFlowContext = new RideFlowContext();
    when(rideTrackerService.endRide(eq(rideId), any(RideTracker.class))).thenAnswer((Answer<RideTracker>) invocation -> (RideTracker) invocation.getArguments()[1]);
    when(airportService.getAirportForLocation(anyDouble(), anyDouble())).thenReturn(Optional.empty());
    when(rideDslRepository.findNextRide(eq(dispatchContext.getCandidate().getId()))).thenReturn(nextRide);
    when(contextAccess.read(anyString())).thenReturn(new StubStateMachineContext(new HashMap<>(ImmutableMap.of(
      "flowContext", nextFlowContext
    ))));
    when(nextRide.getId()).thenReturn(2L);

    testedInstance.execute(context);

    assertEquals(completedOn, nextFlowContext.getAcceptedOn());
  }

  @Test
  public void executeClearsStackedRegistry() {
    final long rideId = 1L;
    final Date completedOn = new Date();
    final Ride ride = new Ride();
    final StubStateContext context = createContext(rideId, ride, completedOn);
    final DispatchContext dispatchContext = context.getExtendedState().get("dispatchContext", DispatchContext.class);
    when(rideTrackerService.endRide(eq(rideId), any(RideTracker.class))).thenAnswer((Answer<RideTracker>) invocation -> (RideTracker) invocation.getArguments()[1]);
    when(airportService.getAirportForLocation(anyDouble(), anyDouble())).thenReturn(Optional.empty());

    testedInstance.execute(context);

    verify(stackedDriverRegistry, times(1)).removeFromStack(eq(dispatchContext.getCandidate().getId()));
  }

  @Test
  public void executeTriggersJobs() throws ServerError {
    final long rideId = 1L;
    final Date completedOn = new Date();
    final Ride ride = new Ride();
    final StubStateContext context = createContext(rideId, ride, completedOn);
    when(rideTrackerService.endRide(eq(rideId), any(RideTracker.class))).thenAnswer((Answer<RideTracker>) invocation -> (RideTracker) invocation.getArguments()[1]);
    when(airportService.getAirportForLocation(anyDouble(), anyDouble())).thenReturn(Optional.empty());

    testedInstance.execute(context);

    verify(schedulerService, atLeastOnce()).triggerJob(eq(RideSummaryJob.class), eq(Long.toString(rideId)), anyString(), anyMap());
  }

  private StubStateContext createContext(long rideId, Ride ride, Date completedOn) {
    when(rideLoadService.findOneForUpdateWithRetry(eq(rideId))).thenReturn(ride);
    final StubStateContext context = new StubStateContext();
    final DispatchContext dispatchContext = new DispatchContext();
    final DispatchCandidate candidate = new DispatchCandidate();
    candidate.setId(1L);
    dispatchContext.setCandidate(candidate);
    context.getStateMachine().getExtendedState().getVariables().putAll(
      ImmutableMap.of(
        "requestContext", new RideRequestContext(),
        "flowContext", new RideFlowContext(),
        "dispatchContext", dispatchContext,
        "rideId", rideId
      )
    );
    final RideEndLocation rideEndLocation = new RideEndLocation();
    final double endLocationLat = 34.681918;
    final double endLocationLong = -97.6465165;
    rideEndLocation.setEndLocationLat(endLocationLat);
    rideEndLocation.setEndLocationLong(endLocationLong);
    context.addMessageHeader("endLocation", rideEndLocation);
    context.addMessageHeader("completedOn", completedOn);
    context.addMessageHeader("result", deferredResult);
    return context;
  }
}