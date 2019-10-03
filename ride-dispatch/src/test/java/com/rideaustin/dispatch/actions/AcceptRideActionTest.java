package com.rideaustin.dispatch.actions;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.DispatchContextAssert;
import com.rideaustin.RideFlowContextAssert;
import com.rideaustin.model.Session;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Car;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.ActiveDriverDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.service.ActiveDriverLocationService;
import com.rideaustin.service.CurrentSessionService;
import com.rideaustin.service.RequestedDriversRegistry;
import com.rideaustin.service.location.enums.LocationType;
import com.rideaustin.service.model.ActiveDriverInfo;
import com.rideaustin.service.model.ConsecutiveDeclinedRequestsData;
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.service.model.context.RideFlowContext;
import com.rideaustin.service.model.context.RideRequestContext;
import com.rideaustin.service.user.CarTypesCache;
import com.rideaustin.service.user.CarTypesUtils;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.utils.dispatch.StateMachineUtils;

public class AcceptRideActionTest extends PersistingContextSupport {

  @Mock
  private CurrentSessionService sessionService;
  @Mock
  private ActiveDriverDslRepository activeDriverDslRepository;
  @Mock
  private RequestedDriversRegistry requestedDriversRegistry;
  @Mock
  private RideDslRepository rideDslRepository;
  @Mock
  private ApplicationEventPublisher publisher;
  @Mock
  private ActiveDriverLocationService activeDriverLocationService;
  @Mock
  private CarTypesCache carTypesCache;

  @InjectMocks
  private AcceptRideAction testedInstance;

  private final static long RIDE_ID = 1L;
  private final static long ACTIVE_DRIVER_ID = 2L;
  private final static long DRIVER_ID = 3L;
  private final static long USER_ID = 4L;
  private final static long SESSION_ID = 5L;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    testedInstance = new AcceptRideAction();
    MockitoAnnotations.initMocks(this);
    CarTypesUtils.setCarTypesCache(carTypesCache);

    context.addMessageHeader("userId", 1L);
    dispatchContext = new DispatchContext();
    dispatchContext.setId(RIDE_ID);
    DispatchCandidate candidate = new DispatchCandidate(ACTIVE_DRIVER_ID, DRIVER_ID,"LCNS", USER_ID, ActiveDriverStatus.AVAILABLE);
    dispatchContext.setCandidate(candidate);
    requestContext = new RideRequestContext();
    requestContext.setRequestedCarTypeCategory(TestUtils.REGULAR);
    flowContext = new RideFlowContext();
    StateMachineUtils.updateDispatchContext(context, dispatchContext, persister, environment);
    StateMachineUtils.updateFlowContext(context, flowContext, persister, environment);
    StateMachineUtils.updateRequestContext(context, requestContext, persister, environment);
  }

  @Test
  public void testExecuteFillsFlowContextWithoutSession() {
    ActiveDriver activeDriver = new ActiveDriver();
    activeDriver.setId(ACTIVE_DRIVER_ID);
    when(activeDriverDslRepository.findById(anyLong())).thenReturn(activeDriver);

    OnlineDriverDto onlineDriverDto = new OnlineDriverDto(ACTIVE_DRIVER_ID, ActiveDriverStatus.AVAILABLE, 1L, null,
      null, null, 1L, null, null, new ConsecutiveDeclinedRequestsData(ImmutableMap.of(TestUtils.REGULAR, 1)), null);
    when(activeDriverLocationService.getById(anyLong(), eq(LocationType.ACTIVE_DRIVER))).thenReturn(onlineDriverDto);

    Date executionTime = new Date();
    testedInstance.execute(context);

    RideFlowContextAssert.assertThat(flowContext)
      .hasAcceptedOn(executionTime)
      .hasDriver(ACTIVE_DRIVER_ID)
      .hasNoSession();
  }

  @Test
  public void testExecuteFillsFlowContextWithSession() {
    ActiveDriver activeDriver = new ActiveDriver();
    activeDriver.setId(ACTIVE_DRIVER_ID);
    when(activeDriverDslRepository.findById(anyLong())).thenReturn(activeDriver);
    OnlineDriverDto onlineDriverDto = new OnlineDriverDto(ACTIVE_DRIVER_ID, ActiveDriverStatus.AVAILABLE, 1L, null,
      null, null, 1L, null, null, new ConsecutiveDeclinedRequestsData(ImmutableMap.of(TestUtils.REGULAR, 1)), null);
    when(activeDriverLocationService.getById(anyLong(), eq(LocationType.ACTIVE_DRIVER))).thenReturn(onlineDriverDto);
    Session session = new Session();
    session.setId(SESSION_ID);
    when(sessionService.getCurrentSession(anyLong())).thenReturn(session);

    Date executionTime = new Date();
    testedInstance.execute(context);

    RideFlowContextAssert.assertThat(flowContext)
      .hasAcceptedOn(executionTime)
      .hasDriver(ACTIVE_DRIVER_ID)
      .hasSession(SESSION_ID);
  }

  @Test
  public void testExecuteUpdatesRideStatus() {
    ActiveDriver activeDriver = new ActiveDriver();
    activeDriver.setId(ACTIVE_DRIVER_ID);
    when(activeDriverDslRepository.findById(anyLong())).thenReturn(activeDriver);
    OnlineDriverDto onlineDriverDto = new OnlineDriverDto(ACTIVE_DRIVER_ID, ActiveDriverStatus.AVAILABLE, 1L, null,
      null, null, 1L, null, null, new ConsecutiveDeclinedRequestsData(ImmutableMap.of(TestUtils.REGULAR, 1)), null);
    when(activeDriverLocationService.getById(anyLong(), eq(LocationType.ACTIVE_DRIVER))).thenReturn(onlineDriverDto);

    testedInstance.execute(context);

    verify(rideDslRepository, times(1)).acceptRide(RIDE_ID, activeDriver);
  }

  @Test
  public void testExecuteUpdatesActiveDriver() {
    ActiveDriver activeDriver = new ActiveDriver();
    activeDriver.setId(ACTIVE_DRIVER_ID);
    when(activeDriverDslRepository.findById(anyLong())).thenReturn(activeDriver);
    OnlineDriverDto onlineDriverDto = new OnlineDriverDto(ACTIVE_DRIVER_ID, ActiveDriverStatus.AVAILABLE, 1L, null,
      null, null, 1L, null, null, new ConsecutiveDeclinedRequestsData(ImmutableMap.of(TestUtils.REGULAR, 1)), null);
    when(activeDriverLocationService.getById(anyLong(), eq(LocationType.ACTIVE_DRIVER))).thenReturn(onlineDriverDto);

    testedInstance.execute(context);

    verify(activeDriverDslRepository, times(1)).setAvailableDriverAsRiding(activeDriver.getId());
    verify(requestedDriversRegistry, times(1)).remove(ACTIVE_DRIVER_ID);
  }

  @Test
  public void testExecuteUpdatesDispatchContext() {
    ActiveDriver activeDriver = new ActiveDriver();
    activeDriver.setId(ACTIVE_DRIVER_ID);
    when(activeDriverDslRepository.findById(anyLong())).thenReturn(activeDriver);
    Driver driver = new Driver();
    driver.setUser(new User());
    OnlineDriverDto onlineDriverDto = new OnlineDriverDto(new ActiveDriverInfo(ACTIVE_DRIVER_ID, driver, new Car(), 1L));
    when(activeDriverLocationService.getById(ACTIVE_DRIVER_ID, LocationType.ACTIVE_DRIVER)).thenReturn(onlineDriverDto);

    testedInstance.execute(context);

    DispatchContextAssert.assertThat(dispatchContext)
      .isAccepted();
  }

}