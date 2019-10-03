package com.rideaustin.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.statemachine.StateMachinePersist;

import com.rideaustin.StubStateMachineContext;
import com.rideaustin.model.City;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.CityDriverType;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.ride.RideQueueToken;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.RiderCard;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.ActiveDriverDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.repo.dsl.RideQueueTokenDslRepository;
import com.rideaustin.repo.dsl.RiderDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.PendingPaymentException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.DeeplinkDto;
import com.rideaustin.rest.model.RideEndLocation;
import com.rideaustin.rest.model.RideRequestParams;
import com.rideaustin.rest.model.RideStartLocation;
import com.rideaustin.service.city.CityValidationService;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.payment.PendingPaymentService;
import com.rideaustin.service.ride.DriverTypeService;
import com.rideaustin.service.user.DriverTypeCache;
import com.rideaustin.service.user.DriverTypeUtils;

public class RideRequestingServiceTest {

  private final User user = new User();
  @Mock
  private CityCache cityCache;
  @Mock
  private DriverTypeService driverTypeService;
  @Mock
  private CityValidationService cityValidationService;
  @Mock
  private RideService rideService;
  @Mock
  private PendingPaymentService pendingPaymentService;
  @Mock
  private RideFlowService rideFlowService;
  @Mock
  private CurrentUserService currentUserService;
  @Mock
  private ActiveDriverDslRepository activeDriverDslRepository;
  @Mock
  private RideDslRepository rideDslRepository;
  @Mock
  private RiderDslRepository riderDslRepository;
  @Mock
  private RideQueueTokenDslRepository rideQueueTokenDslRepository;
  @Mock
  private StateMachinePersist<States, Events, String> contextAccess;
  @Mock
  private Environment environment;
  @Mock
  private DriverTypeCache driverTypeCache;
  
  private RideRequestingService testedInstance;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    DriverTypeUtils.setDriverTypeCache(driverTypeCache);

    testedInstance = new RideRequestingService(cityCache, driverTypeService, cityValidationService, rideService,
      pendingPaymentService, rideFlowService, currentUserService, activeDriverDslRepository, rideDslRepository,
      riderDslRepository, rideQueueTokenDslRepository, contextAccess, environment);
  }

  @Test
  public void requestRideAsRiderThrowsExceptionOnNonEnglishComment() throws RideAustinException {
    final RideRequestParams params = new RideRequestParams();
    params.setComment("Привет");

    expectedException.expectMessage("Your comment seems to be unreadable or uninformative to driver");

    testedInstance.requestRideAsRider(null, null, null, false, 1L, params);
  }

  @Test
  public void requestRideAsRiderThrowsExceptionOnExistingPendingPayments() throws RideAustinException {
    setupRider(false);

    expectedException.expect(PendingPaymentException.class);
    expectedException.expectMessage("Request was blocked due to pending payment.");

    testedInstance.requestRideAsRider(null, null, null, false, 1L, new RideRequestParams());
  }

  @Test(expected = BadRequestException.class)
  public void requestRideAsRiderValidatesCity() throws RideAustinException {
    setupRider(true);
    doThrow(new BadRequestException("Exception")).when(cityValidationService).validateCity(any(RideStartLocation.class), any(CityDriverType.class), anyLong());

    final long cityId = 1L;
    final RideStartLocation startLocation = createStartLocation();
    testedInstance.requestRideAsRider(startLocation, null, null, false, cityId, new RideRequestParams());

    verify(cityValidationService).validateCity(eq(startLocation), isNull(CityDriverType.class), eq(cityId));
  }

  @Test
  public void requestRideAsRiderThrowsErrorWhenDriverTypeNotFound() throws RideAustinException {
    setupRider(true);
    final long cityId = 1L;
    final RideStartLocation startLocation = createStartLocation();
    final RideRequestParams params = new RideRequestParams();
    params.setDriverType("WOMEN_ONLY");
    when(driverTypeService.isDriverTypeExist(params.getDriverType())).thenReturn(false);

    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("Driver type not found");

    testedInstance.requestRideAsRider(startLocation, null, null, false, cityId, params);
  }

  @Test
  public void requestRideAsRiderThrowsErrorWhenDriverTypeNotSupported() throws RideAustinException {
    setupRider(true);
    final long cityId = 1L;
    final RideStartLocation startLocation = createStartLocation();
    final RideRequestParams params = new RideRequestParams();
    params.setDriverType("WOMEN_ONLY");
    when(driverTypeService.isDriverTypeExist(params.getDriverType())).thenReturn(true);
    when(driverTypeService.isCitySupportDriverType(params.getDriverType(), cityId)).thenReturn(false);

    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage(String.format("Current city does not support %s", params.getDriverType()));

    testedInstance.requestRideAsRider(startLocation, null, null, false, cityId, params);
  }

  @Test
  public void requestRideAsRiderThrowsErrorWhenUserIsDisabled() throws RideAustinException {
    setupRider(true);
    final long cityId = 1L;
    final RideStartLocation startLocation = createStartLocation();
    final RideRequestParams params = new RideRequestParams();
    params.setDriverType("WOMEN_ONLY");
    when(driverTypeService.isDriverTypeExist(params.getDriverType())).thenReturn(true);
    when(driverTypeService.isCitySupportDriverType(params.getDriverType(), cityId)).thenReturn(true);
    user.setUserEnabled(false);

    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("Your account is not active - please contact support@example.com.");

    testedInstance.requestRideAsRider(startLocation, null, null, false, cityId, params);
  }

  @Test
  public void requestRideAsRiderThrowsErrorWhenPaymentMethodIsAbsent() throws RideAustinException {
    final Rider rider = setupRider(true);
    final long cityId = 1L;
    final RideStartLocation startLocation = createStartLocation();
    final RideRequestParams params = new RideRequestParams();
    rider.setPrimaryCard(null);
    user.setUserEnabled(true);
    rider.setActive(true);
    params.setApplePayToken(null);

    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("Please setup a payment method before requesting a ride");

    testedInstance.requestRideAsRider(startLocation, null, null, false, cityId, params);
  }

  @Test
  public void requestRideAsRiderThrowsErrorWhenUserIsActiveDriver() throws RideAustinException {
    final Rider rider = setupRider(true);
    final long cityId = 1L;
    final RideStartLocation startLocation = createStartLocation();
    final RideRequestParams params = new RideRequestParams();
    rider.setPrimaryCard(new RiderCard());
    user.setUserEnabled(true);
    rider.setActive(true);
    user.addAvatar(new Driver());
    when(activeDriverDslRepository.findByUserAndNotInactive(user)).thenReturn(new ActiveDriver());

    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("Active drivers cannot request rides");

    testedInstance.requestRideAsRider(startLocation, null, null, false, cityId, params);
  }

  @Test
  public void requestRideAsRiderThrowsErrorWhenMoreThanOneRideIsInProgress() throws Exception {
    final Rider rider = setupRider(true);
    final long cityId = 1L;
    final RideStartLocation startLocation = createStartLocation();
    final RideRequestParams params = new RideRequestParams();
    rider.setPrimaryCard(new RiderCard());
    user.setUserEnabled(true);
    rider.setActive(true);
    final Ride ride = new Ride();
    ride.setId(1L);
    when(rideDslRepository.findByRiderAndStatus(rider, RideStatus.ONGOING_RIDER_STATUSES)).thenReturn(Collections.singletonList(ride));
    when(contextAccess.read(anyString())).thenReturn(new StubStateMachineContext(new HashMap<>(), States.REQUESTED));

    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("Rider has another ongoing ride");

    testedInstance.requestRideAsRider(startLocation, null, null, false, cityId, params);
  }

  @Test
  public void requestRideAsApiClientReturnsInfo() throws RideAustinException {
    final RideStartLocation startLocation = new RideStartLocation();
    final RideEndLocation endLocation = new RideEndLocation();
    final String carCategory = "REGULAR";
    final long cityId = 1L;
    final RideQueueToken queueToken = new RideQueueToken();
    queueToken.setToken("ABC");
    when(rideFlowService.requestRide(startLocation, endLocation, carCategory, cityId)).thenReturn(queueToken);
    final City city = new City();
    city.setAppStoreLink("URL");
    city.setPlayStoreLink("URL1");
    when(cityCache.getCity(cityId)).thenReturn(city);

    final DeeplinkDto result = testedInstance.requestRideAsApiClient(startLocation, endLocation, carCategory, cityId);

    assertEquals(city.getPlayStoreLink(), result.getPlayLink());
    assertEquals(city.getAppStoreLink(), result.getAppStoreLink());
    assertEquals(queueToken.getToken(), result.getToken());
  }

  private RideStartLocation createStartLocation() {
    final RideStartLocation startLocation = new RideStartLocation();
    startLocation.setStartLocationLat(34.1618618);
    startLocation.setStartLocationLong(-97.941891);
    return startLocation;
  }

  private Rider setupRider(boolean pendingPaymentProcessed) throws RideAustinException {
    when(currentUserService.getUser()).thenReturn(user);
    user.addAvatar(new Rider());
    when(riderDslRepository.findByUserWithDependencies(any(User.class))).thenReturn(Collections.singletonList(user.getAvatar(Rider.class)));
    when(pendingPaymentService.handlePendingPayments(any(Rider.class))).thenReturn(pendingPaymentProcessed);
    return user.getAvatar(Rider.class);
  }
}