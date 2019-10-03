package com.rideaustin.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.EnumState;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.request.async.DeferredResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.maps.model.LatLng;
import com.rideaustin.StubStateContext.StubExtendedState;
import com.rideaustin.dispatch.service.RideFlowStateMachineProvider;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.CancellationReason;
import com.rideaustin.model.redis.AreaGeometry;
import com.rideaustin.model.redis.RedisSurgeArea;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.ride.DriverType;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.ride.RideQueueToken;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.repo.dsl.RideQueueTokenDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.rest.model.RideEndLocation;
import com.rideaustin.rest.model.RideRequestParams;
import com.rideaustin.rest.model.RideStartLocation;
import com.rideaustin.service.config.RideDestinationUpdateConfig;
import com.rideaustin.service.config.RideDispatchServiceConfig;
import com.rideaustin.service.config.RidePaymentConfig;
import com.rideaustin.service.eligibility.RiderEligibilityCheckContext;
import com.rideaustin.service.eligibility.RiderEligibilityCheckService;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.RideFlowContext;
import com.rideaustin.service.model.context.RideRequestContext;
import com.rideaustin.service.ride.CarTypeRequestHandler;
import com.rideaustin.service.ride.DriverTypeService;
import com.rideaustin.service.surgepricing.SurgePricingService;
import com.rideaustin.service.user.CarTypesCache;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class RideFlowServiceTest {

  @Mock
  private RideDslRepository rideDslRepository;
  @Mock
  private RideQueueTokenDslRepository rideQueueTokenDslRepository;
  @Mock
  private CurrentUserService currentUserService;
  @Mock
  private RideService rideService;
  @Mock
  private FareEstimateService fareEstimateService;
  @Mock
  private CurrentSessionService currentSessionService;
  @Mock
  private SurgePricingService surgePricingService;
  @Mock
  private PaymentJobService paymentJobService;
  @Mock
  private RiderEligibilityCheckService eligibilityCheckService;
  @Mock
  private CarTypesCache carTypesCache;
  @Mock
  private DriverTypeService driverTypeService;
  @Mock
  private ActiveDriversService activeDriversService;
  @Mock
  private CancellationFeedbackService cancellationFeedbackService;
  @Mock
  private RequestedDriversRegistry requestedDriversRegistry;
  @Mock
  private RidePreauthorizationService preauthorizationService;
  @Mock
  private RideFlowStateMachineProvider machineProvider;
  @Mock
  private RideFlowServiceErrorProvider errorProvider;
  @Mock
  private RideDispatchServiceConfig dispatchConfig;
  @Mock
  private RidePaymentConfig paymentConfig;
  @Mock
  private RideDestinationUpdateConfig destinationUpdateConfig;
  @Mock
  private BeanFactory beanFactory;
  @Mock
  private Environment environment;
  @Mock
  private StateMachine<States, Events> machine;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private ObjectMapper mapper = new ObjectMapper();

  private RideFlowService testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new RideFlowService(rideDslRepository, rideQueueTokenDslRepository, currentUserService, rideService,
      fareEstimateService, currentSessionService, surgePricingService, paymentJobService, eligibilityCheckService, carTypesCache,
      driverTypeService, activeDriversService, cancellationFeedbackService, requestedDriversRegistry, preauthorizationService,
      machineProvider, errorProvider, dispatchConfig, paymentConfig, destinationUpdateConfig, mapper, beanFactory, environment);
  }

  @Test
  public void requestRideAsRiderCallsSpecificCarTypeHandler() throws RideAustinException {
    final String carCategory = "REGULAR";
    final CarType carType = new CarType();
    carType.setConfiguration("{\"requestHandlerClass\":\"com.rideaustin.service.RideFlowServiceTest$TestRequestHandlerClass\"}");
    when(carTypesCache.getCarType(carCategory)).thenReturn(carType);
    final TestRequestHandlerClass handler = new TestRequestHandlerClass();
    when(beanFactory.getBean(TestRequestHandlerClass.class)).thenReturn(handler);
    final RideStartLocation startLocation = setupStartLocation();
    when(surgePricingService.getSurgeAreaByCarType(anyDouble(), anyDouble(), anyLong(), any(CarType.class))).thenReturn(Optional.empty());
    when(fareEstimateService.estimateFare(any(Ride.class))).thenReturn(Optional.empty());

    final Ride requestedRide = testedInstance.requestRide(new Rider(), startLocation, new RideEndLocation(), carCategory,
      false, 1L, new RideRequestParams());

    assertTrue(handler.isHandled());
  }

  @Test
  public void requestRideAsRiderThrowsErrorOnAbsentDriverType() throws RideAustinException {
    final String carCategory = "REGULAR";
    when(carTypesCache.getCarType(carCategory)).thenReturn(new CarType());
    final RideStartLocation startLocation = setupStartLocation();
    when(surgePricingService.getSurgeAreaByCarType(anyDouble(), anyDouble(), anyLong(), any(CarType.class))).thenReturn(Optional.empty());
    when(fareEstimateService.estimateFare(any(Ride.class))).thenReturn(Optional.empty());
    final RideRequestParams requestParams = new RideRequestParams();
    requestParams.setDriverType("DIRECT_CONNECT");
    when(driverTypeService.getOne(requestParams.getDriverType())).thenReturn(null);

    expectedException.expect(ServerError.class);
    expectedException.expectMessage(String.format("Driver type %s not found", requestParams.getDriverType()));

    final Ride requestedRide = testedInstance.requestRide(new Rider(), startLocation, new RideEndLocation(), carCategory,
      false, 1L, requestParams);
  }

  @Test
  public void requestRideAsRiderCallsEligibilityCheck() throws RideAustinException {
    final String carCategory = "REGULAR";
    final CarType carType = new CarType();
    final DriverType driverType = new DriverType();
    driverType.setBitmask(1);
    when(carTypesCache.getCarType(carCategory)).thenReturn(carType);
    final RideStartLocation startLocation = setupStartLocation();
    when(surgePricingService.getSurgeAreaByCarType(anyDouble(), anyDouble(), anyLong(), any(CarType.class))).thenReturn(Optional.empty());
    when(fareEstimateService.estimateFare(any(Ride.class))).thenReturn(Optional.empty());
    final RideRequestParams requestParams = new RideRequestParams();
    requestParams.setDriverType("DIRECT_CONNECT");
    when(driverTypeService.getOne(requestParams.getDriverType())).thenReturn(driverType);
    final Rider rider = new Rider();
    final long cityId = 1L;

    final Ride requestedRide = testedInstance.requestRide(rider, startLocation, new RideEndLocation(), carCategory,
      false, cityId, requestParams);

    verify(eligibilityCheckService).check(argThat(new BaseMatcher<RiderEligibilityCheckContext>() {
      @Override
      public boolean matches(Object o) {
        final RiderEligibilityCheckContext context = (RiderEligibilityCheckContext) o;
        return context.getRider().equals(rider)
          && context.getParams().get(RiderEligibilityCheckContext.CITY).equals(cityId)
          && context.getParams().get(RiderEligibilityCheckContext.CAR_CATEGORY).equals(carType)
          && ((Collection) context.getParams().get(RiderEligibilityCheckContext.DRIVER_TYPE)).contains(driverType);
      }

      @Override
      public void describeTo(Description description) {

      }
    }), eq(cityId));
  }

  @Test
  public void requestRideAsRiderThrowsErrorWhenSurgeIsRequired() throws RideAustinException {
    final String carCategory = "REGULAR";
    final CarType carType = new CarType();
    when(carTypesCache.getCarType(carCategory)).thenReturn(carType);
    final RideStartLocation startLocation = setupStartLocation();
    when(surgePricingService.getSurgeAreaByCarType(anyDouble(), anyDouble(), anyLong(), any(CarType.class))).thenReturn(Optional.empty());
    when(fareEstimateService.estimateFare(any(Ride.class))).thenReturn(Optional.empty());
    when(surgePricingService.isSurgeMandatory(anyDouble(), anyDouble(), any(CarType.class), anyLong())).thenReturn(true);

    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("Priority Fare is active - please request again");

    final Ride requestedRide = testedInstance.requestRide(new Rider(), startLocation, new RideEndLocation(), carCategory,
      false, 1L, new RideRequestParams());

  }

  @Test
  public void requestRideAsRiderSetsSurgeFactor() throws RideAustinException {
    final BigDecimal surgeFactor = BigDecimal.valueOf(2);
    final String carCategory = "REGULAR";
    final CarType carType = new CarType();
    when(carTypesCache.getCarType(carCategory)).thenReturn(carType);
    final RideStartLocation startLocation = setupStartLocation();
    when(surgePricingService.getSurgeAreaByCarType(anyDouble(), anyDouble(), anyLong(), any(CarType.class))).thenReturn(Optional.empty());
    when(fareEstimateService.estimateFare(any(Ride.class))).thenReturn(Optional.empty());
    when(surgePricingService.isSurgeMandatory(anyDouble(), anyDouble(), any(CarType.class), anyLong())).thenReturn(true);
    when(surgePricingService.getSurgeFactor(any(LatLng.class), any(CarType.class), anyLong())).thenReturn(surgeFactor);
    when(rideDslRepository.save(any(Ride.class))).thenAnswer((Answer<Ride>) invocation -> (Ride) invocation.getArguments()[0]);

    final Ride requestedRide = testedInstance.requestRide(new Rider(), startLocation, new RideEndLocation(), carCategory,
      true, 1L, new RideRequestParams());

    assertEquals(surgeFactor, requestedRide.getSurgeFactor());
  }

  @Test
  public void requestRideAsDispatcherSetsRiderOverride() throws RideAustinException {
    final String carCategory = "REGULAR";
    final CarType carType = new CarType();
    when(carTypesCache.getCarType(carCategory)).thenReturn(carType);
    final RideStartLocation startLocation = setupStartLocation();
    when(surgePricingService.getSurgeAreaByCarType(anyDouble(), anyDouble(), anyLong(), any(CarType.class))).thenReturn(Optional.empty());
    when(fareEstimateService.estimateFare(any(Ride.class))).thenReturn(Optional.empty());
    when(rideDslRepository.save(any(Ride.class))).thenAnswer((Answer<Ride>) invocation -> (Ride) invocation.getArguments()[0]);
    final Rider rider = new Rider();
    final User user = new User();
    user.setFirstname("A");
    user.setLastname("B");
    user.setPhoneNumber("+15120000000");
    rider.setDispatcherAccount(true);
    rider.setUser(user);
    final RideRequestParams params = new RideRequestParams();
    params.setRiderFirstName("OFirst");
    params.setRiderLastName("OLast");
    params.setRiderPhoneNumber("+15125555555");

    final Ride requestedRide = testedInstance.requestRide(rider, startLocation, new RideEndLocation(), carCategory,
      false, 1L, params);

    assertEquals(params.getRiderFirstName(), requestedRide.getRiderOverride().getFirstName());
    assertEquals(params.getRiderLastName(), requestedRide.getRiderOverride().getLastName());
    assertEquals(params.getRiderPhoneNumber(), requestedRide.getRiderOverride().getPhoneNumber());
  }

  @Test
  public void requestRideAsRiderStartsMachineAfterTransactionCommit() throws RideAustinException {
    final String carCategory = "REGULAR";
    final CarType carType = new CarType();
    when(carTypesCache.getCarType(carCategory)).thenReturn(carType);
    final RideStartLocation startLocation = setupStartLocation();
    when(surgePricingService.getSurgeAreaByCarType(anyDouble(), anyDouble(), anyLong(), any(CarType.class))).thenReturn(Optional.empty());
    when(fareEstimateService.estimateFare(any(Ride.class))).thenReturn(Optional.empty());
    when(rideDslRepository.save(any(Ride.class))).thenAnswer((Answer<Ride>) invocation -> (Ride) invocation.getArguments()[0]);
    doCallRealMethod().when(paymentJobService).afterCommit(any(PaymentJobService.Action.class));
    TransactionSynchronizationManager.initSynchronization();
    when(machineProvider.createMachine(any(RideRequestContext.class))).thenReturn(machine);

    final Ride requestedRide = testedInstance.requestRide(new Rider(), startLocation, new RideEndLocation(), carCategory,
      false, 1L, new RideRequestParams());
    TransactionSynchronizationManager.getSynchronizations().get(0).afterCommit();

    TransactionSynchronizationManager.clearSynchronization();

    verify(machine).start();
  }

  @Test
  public void requestRideAsApiClientSetsSurgeFactor() throws RideAustinException {
    final BigDecimal surgeFactor = BigDecimal.valueOf(2);
    final String carCategory = "REGULAR";
    final CarType carType = new CarType();
    when(carTypesCache.getCarType(carCategory)).thenReturn(carType);
    final RideStartLocation startLocation = setupStartLocation();
    final RedisSurgeArea redisSurgeArea = new RedisSurgeArea();
    final AreaGeometry areaGeometry = new AreaGeometry();
    areaGeometry.setId(1L);
    redisSurgeArea.setAreaGeometry(areaGeometry);
    when(surgePricingService.getSurgeAreaByCarType(anyDouble(), anyDouble(), anyLong(), any(CarType.class))).thenReturn(Optional.of(redisSurgeArea));
    when(fareEstimateService.estimateFare(any(Ride.class))).thenReturn(Optional.empty());
    when(surgePricingService.getSurgeFactor(any(LatLng.class), any(CarType.class), anyLong())).thenReturn(surgeFactor);
    when(rideDslRepository.save(any(Ride.class))).thenAnswer((Answer<Ride>) invocation -> (Ride) invocation.getArguments()[0]);

    final RideQueueToken token = testedInstance.requestRide(startLocation, new RideEndLocation(), carCategory, 1L);

    verify(rideDslRepository).save(argThat(new BaseMatcher<Ride>() {
      @Override
      public boolean matches(Object o) {
        final Ride savedRide = (Ride) o;
        return savedRide.getSurgeFactor().equals(surgeFactor);
      }

      @Override
      public void describeTo(Description description) {

      }
    }));
  }

  @Test
  public void associateRideThrowsErrorOnAbsentToken() throws RideAustinException {
    final String token = "13213";
    when(rideQueueTokenDslRepository.findOne(token)).thenReturn(null);

    expectedException.expect(NotFoundException.class);
    expectedException.expectMessage("Ride request not found");

    testedInstance.associateRide(token);
  }

  @Test
  public void associateRideThrowsErrorOnExpiredToken() throws RideAustinException {
    final String token = "13213";
    final RideQueueToken queueToken = new RideQueueToken();
    queueToken.setExpired(true);
    when(rideQueueTokenDslRepository.findOne(token)).thenReturn(queueToken);

    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("Ride request has already expired, please try again");

    testedInstance.associateRide(token);
  }

  @Test
  public void associateRideExpiresToken() throws RideAustinException {
    final String token = "13213";
    final RideQueueToken queueToken = new RideQueueToken();
    when(rideQueueTokenDslRepository.findOne(token)).thenReturn(queueToken);
    final Ride ride = new Ride();
    ride.setId(1L);
    ride.setRequestedCarType(new CarType());
    when(rideDslRepository.findOne(anyLong())).thenReturn(ride);
    when(rideDslRepository.save(any(Ride.class))).thenAnswer((Answer<Ride>) invocation -> (Ride) invocation.getArguments()[0]);
    final User user = new User();
    final Rider rider = new Rider();
    user.addAvatar(rider);
    ride.setRider(rider);
    when(currentUserService.getUser()).thenReturn(user);
    when(machineProvider.createMachine(any(RideRequestContext.class))).thenReturn(machine);

    testedInstance.associateRide(token);

    assertTrue(queueToken.isExpired());
    verify(machine).start();
  }

  @Test
  public void acknowledgeHandshakeProxiesEvent() {
    when(currentUserService.getUser()).thenReturn(new User());
    final long rideId = 1L;

    testedInstance.acknowledgeHandshake(rideId, new DeferredResult<>());

    verify(machineProvider).sendProxiedEvent(eq(rideId), eq(Events.HANDSHAKE_ACKNOWLEDGE), any(MessageHeaders.class));
    verify(machineProvider, never()).restoreMachine(eq(rideId), any(Events.class), any(MessageHeaders.class));
  }

  @Test
  public void acceptRideSendsEventWhenDriverIsRequested() {
    final long rideId = 1L;
    final User user = new User();
    when(currentUserService.getUser()).thenReturn(user);
    when(activeDriversService.getActiveDriverByDriver(user)).thenReturn(new ActiveDriver());
    when(requestedDriversRegistry.isRequested(anyLong())).thenReturn(true);
    when(machineProvider.restoreMachine(eq(rideId), eq(Events.DISPATCH_REQUEST_ACCEPT), any(MessageHeaders.class)))
      .thenReturn(Optional.of(machine));
    final StubExtendedState extendedState = new StubExtendedState();
    extendedState.getVariables().put("rideId", rideId);
    when(machine.getExtendedState()).thenReturn(extendedState);
    when(machine.sendEvent(any(GenericMessage.class))).thenReturn(true);

    testedInstance.acceptRide(rideId, new DeferredResult<>());

    verify(machine).sendEvent(argThat(new EventMessageMatcher(Events.DISPATCH_REQUEST_ACCEPT)));
  }

  @Test
  public void acceptRideSetsErrorWhenDriverNotFound() {
    final long rideId = 1L;
    final User user = new User();
    when(currentUserService.getUser()).thenReturn(user);
    when(activeDriversService.getActiveDriverByDriver(user)).thenReturn(null);
    final DeferredResult<ResponseEntity<Object>> result = new DeferredResult<>();

    testedInstance.acceptRide(rideId, result);

    assertEquals(HttpStatus.BAD_REQUEST, ((ResponseEntity) result.getResult()).getStatusCode());
    assertEquals("Ride is already redispatched, you can't accept it", ((ResponseEntity) result.getResult()).getBody());
  }

  @Test
  public void acceptRideSetsErrorWhenDriverIsNotRequested() {
    final long rideId = 1L;
    final User user = new User();
    when(currentUserService.getUser()).thenReturn(user);
    when(activeDriversService.getActiveDriverByDriver(user)).thenReturn(new ActiveDriver());
    when(requestedDriversRegistry.isRequested(anyLong())).thenReturn(false);
    final DeferredResult<ResponseEntity<Object>> result = new DeferredResult<>();

    testedInstance.acceptRide(rideId, result);

    assertEquals(HttpStatus.BAD_REQUEST, ((ResponseEntity) result.getResult()).getStatusCode());
    assertEquals("Ride is already redispatched, you can't accept it", ((ResponseEntity) result.getResult()).getBody());
  }

  @Test
  public void acceptRideSetsErrorWhenEventIsNotSent() {
    final long rideId = 1L;
    final User user = new User();
    when(currentUserService.getUser()).thenReturn(user);
    when(activeDriversService.getActiveDriverByDriver(user)).thenReturn(new ActiveDriver());
    when(requestedDriversRegistry.isRequested(anyLong())).thenReturn(true);
    when(machineProvider.restoreMachine(eq(rideId), eq(Events.DISPATCH_REQUEST_ACCEPT), any(MessageHeaders.class)))
      .thenReturn(Optional.of(machine));
    final StubExtendedState extendedState = new StubExtendedState();
    extendedState.getVariables().put("rideId", rideId);
    when(machine.getState()).thenReturn(new EnumState<>(States.DISPATCH_PENDING));
    when(machine.getExtendedState()).thenReturn(extendedState);
    when(machine.sendEvent(any(GenericMessage.class))).thenReturn(false);
    when(errorProvider.errorResultSetter(any(States.class), eq(Events.DISPATCH_REQUEST_ACCEPT))).thenReturn(a -> {});

    testedInstance.acceptRide(rideId, new DeferredResult<>());

    verify(errorProvider).errorResultSetter(any(States.class), eq(Events.DISPATCH_REQUEST_ACCEPT));
  }

  @Test
  public void acceptRideSetsErrorWhenMachineIsNotRestored() {
    final long rideId = 1L;
    final User user = new User();
    when(currentUserService.getUser()).thenReturn(user);
    when(activeDriversService.getActiveDriverByDriver(user)).thenReturn(new ActiveDriver());
    when(requestedDriversRegistry.isRequested(anyLong())).thenReturn(true);
    when(machineProvider.restoreMachine(eq(rideId), eq(Events.DISPATCH_REQUEST_ACCEPT), any(MessageHeaders.class)))
      .thenReturn(Optional.empty());
    final DeferredResult<ResponseEntity<Object>> result = new DeferredResult<>();

    testedInstance.acceptRide(rideId, result);

    assertEquals("Ride state is not found", ((BadRequestException) result.getResult()).getMessage());
  }

  @Test
  public void cancelAsAdminSendsEvent() {
    final long rideId = 1L;
    when(machineProvider.restoreMachine(eq(rideId), eq(Events.ADMIN_CANCEL), any(MessageHeaders.class)))
      .thenReturn(Optional.of(machine));
    final StubExtendedState extendedState = new StubExtendedState();
    extendedState.getVariables().put("rideId", rideId);
    when(machine.getExtendedState()).thenReturn(extendedState);
    when(machine.sendEvent(any(GenericMessage.class))).thenReturn(true);

    testedInstance.cancelAsAdmin(rideId);

    verify(machine).sendEvent(argThat(new EventMessageMatcher(Events.ADMIN_CANCEL)));
  }

  @Test
  public void cancelAsDriverSetsErrorWhenFailedToSubmitFeedback() throws BadRequestException {
    final long rideId = 1L;
    final CancellationReason reason = CancellationReason.NO_SHOW;
    final String comment = "A";
    final BadRequestException error = new BadRequestException("ERROR");
    final DeferredResult<ResponseEntity<Object>> result = new DeferredResult<>();

    doThrow(error)
      .when(cancellationFeedbackService)
      .submit(rideId, reason, AvatarType.DRIVER, comment);
    when(machineProvider.restoreMachine(eq(rideId), eq(Events.DRIVER_CANCEL), any(MessageHeaders.class)))
      .thenReturn(Optional.of(machine));
    final StubExtendedState extendedState = new StubExtendedState();
    extendedState.getVariables().put("rideId", rideId);
    when(machine.getExtendedState()).thenReturn(extendedState);
    when(machine.sendEvent(any(GenericMessage.class))).thenReturn(true);

    testedInstance.cancelAsDriver(rideId, result, reason, comment);

    assertEquals(error, result.getResult());
  }

  @Test
  public void cancelAsDriverSendsMessage() {
    final long rideId = 1L;
    final CancellationReason reason = CancellationReason.NO_SHOW;
    final String comment = "A";
    final DeferredResult<ResponseEntity<Object>> result = new DeferredResult<>();

    when(machineProvider.restoreMachine(eq(rideId), eq(Events.DRIVER_CANCEL), any(MessageHeaders.class)))
      .thenReturn(Optional.of(machine));
    final StubExtendedState extendedState = new StubExtendedState();
    extendedState.getVariables().put("rideId", rideId);
    when(machine.getExtendedState()).thenReturn(extendedState);
    when(machine.sendEvent(any(GenericMessage.class))).thenReturn(true);

    testedInstance.cancelAsDriver(rideId, result, reason, comment);

    verify(machine).sendEvent(argThat(new EventMessageMatcher(Events.DRIVER_CANCEL)));
  }

  @Test
  public void cancelAsRiderSendsEvent() {
    final long rideId = 1L;
    when(machineProvider.restoreMachine(eq(rideId), eq(Events.RIDER_CANCEL), any(MessageHeaders.class)))
      .thenReturn(Optional.of(machine));
    final StubExtendedState extendedState = new StubExtendedState();
    extendedState.getVariables().put("rideId", rideId);
    when(machine.getExtendedState()).thenReturn(extendedState);
    when(machine.sendEvent(any(GenericMessage.class))).thenReturn(true);

    testedInstance.cancelAsRider(rideId, new DeferredResult<>());

    verify(machine).sendEvent(argThat(new EventMessageMatcher(Events.RIDER_CANCEL)));
  }

  @Test
  public void updateDestinationSetsErrorWhenUpdateHitsLimit() {
    final long rideId = 1L;
    final int limit = 3;
    final StubExtendedState extendedState = new StubExtendedState();
    final RideFlowContext flowContext = new RideFlowContext();
    flowContext.setStartedOn(new Date());
    flowContext.setDestinationUpdates(limit);
    extendedState.getVariables().putAll(ImmutableMap.of(
      "rideId", rideId,
      "flowContext", flowContext
    ));
    final User user = new User();
    when(currentUserService.getUser()).thenReturn(user);
    when(machineProvider.restoreMachine(eq(rideId), eq(Events.UPDATE_DESTINATION), any(MessageHeaders.class)))
      .thenReturn(Optional.of(machine));
    when(destinationUpdateConfig.isDestinationUpdateLimited()).thenReturn(true);
    when(destinationUpdateConfig.getDestinationUpdateLimit()).thenReturn(limit);
    when(machine.getExtendedState()).thenReturn(extendedState);
    when(machine.sendEvent(any(GenericMessage.class))).thenReturn(true);
    final DeferredResult<ResponseEntity> result = new DeferredResult<>();

    testedInstance.updateDestination(rideId, new RideEndLocation(), result);

    assertEquals("You can't change your destination more than 3 times while in a ride. Please order another ride",
      ((BadRequestException) result.getResult()).getMessage());
  }

  @Test
  public void updateCommentSendsMessage() {
    final long rideId = 1L;
    when(currentUserService.getUser()).thenReturn(new User());
    when(machineProvider.restoreMachine(eq(rideId), eq(Events.UPDATE_COMMENT), any(MessageHeaders.class)))
      .thenReturn(Optional.of(machine));
    final StubExtendedState extendedState = new StubExtendedState();
    extendedState.getVariables().put("rideId", rideId);
    when(machine.getExtendedState()).thenReturn(extendedState);
    when(machine.sendEvent(any(GenericMessage.class))).thenReturn(true);

    testedInstance.updateComment(rideId, "ABC");

    verify(machine).sendEvent(argThat(new EventMessageMatcher(Events.UPDATE_COMMENT)));
  }

  @Test
  public void declineRideProxiesEvent() {
    final long rideId = 1L;
    when(currentUserService.getUser()).thenReturn(new User());

    testedInstance.declineRide(rideId);

    verify(machineProvider).sendProxiedEvent(eq(rideId), eq(Events.DISPATCH_REQUEST_DECLINE), any(MessageHeaders.class));
    verify(machineProvider, never()).restoreMachine(eq(rideId), any(Events.class), any(MessageHeaders.class));
  }

  @Test
  public void driverReachSendsMessage() {
    final long rideId = 1L;
    when(currentUserService.getUser()).thenReturn(new User());
    when(machineProvider.restoreMachine(eq(rideId), eq(Events.DRIVER_REACH), any(MessageHeaders.class)))
      .thenReturn(Optional.of(machine));
    final StubExtendedState extendedState = new StubExtendedState();
    extendedState.getVariables().put("rideId", rideId);
    when(machine.getExtendedState()).thenReturn(extendedState);
    when(machine.sendEvent(any(GenericMessage.class))).thenReturn(true);

    testedInstance.driverReached(rideId, new DeferredResult<>());

    verify(machine).sendEvent(argThat(new EventMessageMatcher(Events.DRIVER_REACH)));
  }

  @Test
  public void startRideSendsMessage() {
    final long rideId = 1L;
    when(currentUserService.getUser()).thenReturn(new User());
    when(machineProvider.restoreMachine(eq(rideId), eq(Events.START_RIDE), any(MessageHeaders.class)))
      .thenReturn(Optional.of(machine));
    final StubExtendedState extendedState = new StubExtendedState();
    extendedState.getVariables().put("rideId", rideId);
    when(machine.getExtendedState()).thenReturn(extendedState);
    when(machine.sendEvent(any(GenericMessage.class))).thenReturn(true);

    testedInstance.startRide(rideId, new DeferredResult<>());

    verify(machine).sendEvent(argThat(new EventMessageMatcher(Events.START_RIDE)));
  }

  @Test
  public void endRideSendsMessage() {
    final long rideId = 1L;
    when(currentUserService.getUser()).thenReturn(new User());
    when(machineProvider.restoreMachine(eq(rideId), eq(Events.END_RIDE), any(MessageHeaders.class)))
      .thenReturn(Optional.of(machine));
    final StubExtendedState extendedState = new StubExtendedState();
    extendedState.getVariables().put("rideId", rideId);
    when(machine.getExtendedState()).thenReturn(extendedState);
    when(machine.sendEvent(any(GenericMessage.class))).thenReturn(true);

    testedInstance.endRide(rideId, new RideEndLocation(), new DeferredResult<>());

    verify(machine).sendEvent(argThat(new EventMessageMatcher(Events.END_RIDE)));
  }

  private RideStartLocation setupStartLocation() throws RideAustinException {
    final RideStartLocation startLocation = new RideStartLocation();
    startLocation.setStartLocationLat(34.0986416);
    startLocation.setStartLocationLong(-97.9191354);
    startLocation.setStartAddress("Address");
    doAnswer(invocation -> {
      ((Ride) invocation.getArguments()[0]).setStartLocationLat(startLocation.getLat());
      ((Ride) invocation.getArguments()[0]).setStartLocationLong(startLocation.getLng());
      return null;
    }).when(rideService).fillStartLocation(any(Ride.class), eq(startLocation));
    return startLocation;
  }

  public static class TestRequestHandlerClass implements CarTypeRequestHandler {

    @Getter
    private boolean handled = false;

    @Override
    public void handleRequest(User rider, String address, LatLng location, String comment, Long cityId) {
      handled = true;
    }
  }

  @RequiredArgsConstructor
  private static class EventMessageMatcher extends BaseMatcher<GenericMessage<Events>> {

    private final Events expected;

    @Override
    public boolean matches(Object o) {
      return ((GenericMessage<Events>) o).getPayload() == expected;
    }

    @Override
    public void describeTo(Description description) {

    }
  }
}