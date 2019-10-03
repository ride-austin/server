package com.rideaustin.service.ride;

import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.rideaustin.Constants;
import com.rideaustin.clients.configuration.ConfigurationItemService;
import com.rideaustin.model.ConfigurationItem;
import com.rideaustin.model.Session;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.enums.RideUpgradeRequestStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.ride.RideUpgradeRequest;
import com.rideaustin.model.surgepricing.SurgeArea;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.repo.dsl.RideUpgradeRequestDslRepository;
import com.rideaustin.repo.dsl.SurgeAreaDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.ForbiddenException;
import com.rideaustin.rest.model.RiderDto;
import com.rideaustin.service.ActiveDriversService;
import com.rideaustin.service.event.EventsNotificationService;
import com.rideaustin.service.notifications.PushNotificationsFacade;
import com.rideaustin.service.user.CarTypesCache;
import com.rideaustin.service.user.RiderService;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

@RunWith(DataProviderRunner.class)
public class RideUpgradeServiceTest {

  private static final String CAR_CATEGORY = "SUV";
  private static final String SOURCE_CAR_CATEGORY = "REGULAR";
  private static final String INVALID_CAR_CATEGORY = "SUV_";
  private static final String ELIGIBLE_RIDER_VERSION = "RideAustin_3.2.0";
  private static final long DRIVER_ID = 1L;
  private static final long RIDER_ID = 2L;
  private static final int EXPIRATION_TIMEOUT = 45;
  private static final long RIDE_ID = 5L;
  private static final long START_AREA_ID = 9L;

  @Mock
  private SurgeAreaDslRepository surgeAreaDslRepository;
  @Mock
  private RideUpgradeRequestDslRepository repository;
  @Mock
  private CarTypesCache carTypesCache;
  @Mock
  private RiderService riderService;
  @Mock
  private ActiveDriversService activeDriversService;
  @Mock
  private ConfigurationItemService configurationItemService;
  @Mock
  private PushNotificationsFacade notificationsFacade;
  @Mock
  private EventsNotificationService eventsNotificationService;
  @Mock
  private RideDslRepository rideDslRepository;
  @Mock
  private Environment environment;

  private RideUpgradeService testedInstance;

  @Captor
  private ArgumentCaptor<RideUpgradeRequest> requestCaptor;
  @Rule
  public ExpectedException expected = ExpectedException.none();
  private ActiveDriver activeDriver;
  private Ride ride;
  private RideUpgradeRequest request;

  @DataProvider
  public static Object[] notUpgradableStatuses() {
    return EnumSet.complementOf(EnumSet.of(RideStatus.DRIVER_REACHED)).toArray();
  }

  @DataProvider
  public static Object[] invalidConfigs() {
    return new Object[]{
      "{}",
      "{"
    };
  }

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    when(environment.getProperty("ride.upgrade.request.expiration.timeout", Integer.class, 45)).thenReturn(EXPIRATION_TIMEOUT);
    testedInstance = new RideUpgradeService(surgeAreaDslRepository, repository, rideDslRepository, carTypesCache, riderService, activeDriversService,
      configurationItemService, notificationsFacade, eventsNotificationService, new ObjectMapper(), new RideUpgradeService.Config(environment));
    activeDriver = new ActiveDriver();
    Driver driver = new Driver();
    driver.setId(DRIVER_ID);
    activeDriver.setDriver(driver);
    ride = new Ride();
    ride.setId(RIDE_ID);
    Rider rider = new Rider();
    rider.setId(RIDER_ID);
    ride.setRider(rider);
    ride.setActiveDriver(activeDriver);
    ride.setSurgeFactor(Constants.NEUTRAL_SURGE_FACTOR);
    request = RideUpgradeRequest.builder()
      .rideId(RIDE_ID)
      .requestedBy(DRIVER_ID)
      .requestedFrom(RIDER_ID)
      .status(RideUpgradeRequestStatus.REQUESTED)
      .target(CAR_CATEGORY)
      .source(SOURCE_CAR_CATEGORY)
      .surgeFactor(Constants.NEUTRAL_SURGE_FACTOR)
      .build();
  }

  @Test
  public void testRequestUpgradeThrowsExceptionWhenNoActiveDriverIsFound() throws Exception {
    when(activeDriversService.getCurrentActiveDriver()).thenReturn(null);

    expected.expect(BadRequestException.class);
    expected.expectMessage("You can't request ride upgrade while being offline");

    testedInstance.requestUpgrade(CAR_CATEGORY);
  }

  @Test
  public void testRequestUpgradeThrowsExceptionWhenNoActiveRideIsFound() throws Exception {
    when(activeDriversService.getCurrentActiveDriver()).thenReturn(activeDriver);
    when(rideDslRepository.findByDriverAndStatus(eq(activeDriver), anySetOf(RideStatus.class))).thenReturn(null);

    expected.expect(BadRequestException.class);
    expected.expectMessage("You can't request ride upgrade while not in a ride");

    testedInstance.requestUpgrade(CAR_CATEGORY);
  }

  @Test
  @UseDataProvider("notUpgradableStatuses")
  public void testRequestUpgradeThrowsExceptionWhenRideIsNotEligibleToUpgrade(RideStatus status) throws Exception {
    when(activeDriversService.getCurrentActiveDriver()).thenReturn(activeDriver);
    Ride ride = new Ride();
    ride.setStatus(status);
    when(rideDslRepository.findByDriverAndStatus(eq(activeDriver), anySetOf(RideStatus.class))).thenReturn(ride);

    expected.expect(BadRequestException.class);
    expected.expectMessage("Ride can not be upgraded");

    testedInstance.requestUpgrade(CAR_CATEGORY);
  }

  @Test
  public void testRequestUpgradeThrowsExceptionWhenInvalidTargetSubmitted() throws Exception {
    when(activeDriversService.getCurrentActiveDriver()).thenReturn(activeDriver);
    Ride ride = new Ride();
    ride.setStatus(RideStatus.DRIVER_REACHED);
    ride.setRequestedCarType(new CarType());
    when(rideDslRepository.findByDriverAndStatus(eq(activeDriver), anySetOf(RideStatus.class))).thenReturn(ride);
    when(carTypesCache.getCarType(anyString())).thenReturn(null);

    expected.expect(BadRequestException.class);
    expected.expectMessage("Specified car category doesn't exist");

    testedInstance.requestUpgrade(INVALID_CAR_CATEGORY);
  }

  @Test
  public void testRequestUpgradeThrowsExceptionWhenUpgradeIsNotSupported() throws Exception {
    when(activeDriversService.getCurrentActiveDriver()).thenReturn(activeDriver);
    Ride ride = new Ride();
    ride.setStatus(RideStatus.DRIVER_REACHED);
    ride.setRequestedCarType(CarType.builder().carCategory(SOURCE_CAR_CATEGORY).build());
    when(rideDslRepository.findByDriverAndStatus(eq(activeDriver), anySetOf(RideStatus.class))).thenReturn(ride);
    when(configurationItemService.findByKeyAndCityId(anyString(), anyLong())).thenReturn(
      ConfigurationItem.builder()
        .configurationValue("{\"buttonTitle\":\"Ask rider to upgrade to SUV\",\"variants\":[{\"carCategory\":\"REGULAR\",\"validUpgrades\":[\"PREMIUM\"]}]}")
        .build()
    );
    when(carTypesCache.getCarType(CAR_CATEGORY)).thenReturn(CarType.builder().carCategory(CAR_CATEGORY).build());

    expected.expect(BadRequestException.class);
    expected.expectMessage(String.format("Ride upgrade from %s to %s is not supported", SOURCE_CAR_CATEGORY, CAR_CATEGORY));

    testedInstance.requestUpgrade(CAR_CATEGORY);
  }

  @Test
  public void testRequestUpgradeThrowsExceptionWhenConfigIsNotFound() throws Exception {
    when(activeDriversService.getCurrentActiveDriver()).thenReturn(activeDriver);
    Ride ride = new Ride();
    ride.setStatus(RideStatus.DRIVER_REACHED);
    ride.setRequestedCarType(CarType.builder().carCategory(SOURCE_CAR_CATEGORY).build());
    when(rideDslRepository.findByDriverAndStatus(eq(activeDriver), anySetOf(RideStatus.class))).thenReturn(ride);
    when(configurationItemService.findByKeyAndCityId(anyString(), anyLong())).thenReturn(null);
    when(carTypesCache.getCarType(CAR_CATEGORY)).thenReturn(CarType.builder().carCategory(CAR_CATEGORY).build());

    expected.expect(BadRequestException.class);
    expected.expectMessage(String.format("Ride upgrade from %s to %s is not supported", SOURCE_CAR_CATEGORY, CAR_CATEGORY));

    testedInstance.requestUpgrade(CAR_CATEGORY);
  }

  @Test
  @UseDataProvider("invalidConfigs")
  public void testRequestUpgradeThrowsExceptionWhenConfigIsInvalid(String config) throws Exception {
    when(activeDriversService.getCurrentActiveDriver()).thenReturn(activeDriver);
    Ride ride = new Ride();
    ride.setStatus(RideStatus.DRIVER_REACHED);
    ride.setRequestedCarType(CarType.builder().carCategory(SOURCE_CAR_CATEGORY).build());
    when(rideDslRepository.findByDriverAndStatus(eq(activeDriver), anySetOf(RideStatus.class))).thenReturn(ride);
    when(configurationItemService.findByKeyAndCityId(anyString(), anyLong())).thenReturn(ConfigurationItem.builder().configurationValue(config).build());
    when(carTypesCache.getCarType(CAR_CATEGORY)).thenReturn(CarType.builder().carCategory(CAR_CATEGORY).build());

    expected.expect(BadRequestException.class);
    expected.expectMessage(String.format("Ride upgrade from %s to %s is not supported", SOURCE_CAR_CATEGORY, CAR_CATEGORY));

    testedInstance.requestUpgrade(CAR_CATEGORY);
  }

  @Test
  public void testRequestUpgradeThrowsExceptionWhenUpgradeWasAlreadyRequested() throws Exception {
    setupRequest(true);

    expected.expect(BadRequestException.class);
    expected.expectMessage("You can't request upgrade for a ride more than once");

    testedInstance.requestUpgrade(CAR_CATEGORY);
  }

  @Test
  public void testRequestUpgradeSendsNeutralSurgeFactorIfRideIsOutOfSurge() throws Exception {
    setupRequest(false);

    testedInstance.requestUpgrade(CAR_CATEGORY);

    verify(repository, times(1)).save(requestCaptor.capture());
    assertRequest(requestCaptor.getValue());
    verify(notificationsFacade, times(1)).pushRideUpgradeRequest(eq(RIDE_ID),
      eq(ride.getRider().getUser()), eq(SOURCE_CAR_CATEGORY), eq(CAR_CATEGORY), eq(Constants.NEUTRAL_SURGE_FACTOR));
  }

  @Test
  public void testRequestUpgradeSendsRealSurgeFactorIfRideIsInSurge() throws Exception {
    setupRequest(false);
    ride.setStartAreaId(START_AREA_ID);
    BigDecimal surgeFactor = BigDecimal.valueOf(2.0);
    SurgeArea surgeArea = SurgeArea.builder()
      .surgeMapping(ImmutableMap.of(CAR_CATEGORY, surgeFactor))
      .build();
    when(surgeAreaDslRepository.findByAreaGeometry(START_AREA_ID)).thenReturn(
      surgeArea
    );
    ArgumentCaptor<RideUpgradeRequest> requestCaptor = ArgumentCaptor.forClass(RideUpgradeRequest.class);

    testedInstance.requestUpgrade(CAR_CATEGORY);

    verify(repository, times(1)).save(requestCaptor.capture());
    assertRequest(requestCaptor.getValue());
    verify(notificationsFacade, times(1)).pushRideUpgradeRequest(eq(RIDE_ID),
      eq(ride.getRider().getUser()), eq(SOURCE_CAR_CATEGORY), eq(CAR_CATEGORY), eq(surgeFactor));
  }

  @Test
  public void testAcceptRequestReturnsFalseWhenRequestNotFound() throws Exception {
    when(riderService.getCurrentRider()).thenReturn(new RiderDto(RIDER_ID));
    when(repository.findByRiderAndStatus(anyLong(),
      eq(RideUpgradeRequestStatus.REQUESTED), eq(RideUpgradeRequestStatus.CANCELLED))).thenReturn(null);

    boolean result = testedInstance.acceptRequest();

    assertFalse(result);
    verify(repository, never()).save(any(RideUpgradeRequest.class));
  }

  @Test
  public void testAcceptRequestReturnsTrueOnSuccessfulUpdate() throws Exception {
    when(riderService.getCurrentRider()).thenReturn(new RiderDto(RIDER_ID));
    when(repository.findByRiderAndStatus(anyLong(),
      eq(RideUpgradeRequestStatus.REQUESTED), eq(RideUpgradeRequestStatus.CANCELLED), eq(RideUpgradeRequestStatus.EXPIRED))).thenReturn(request);
    when(rideDslRepository.findOne(ride.getId())).thenReturn(ride);

    boolean result = testedInstance.acceptRequest();

    verify(repository, times(1)).save(requestCaptor.capture());
    assertEquals(RideUpgradeRequestStatus.ACCEPTED, requestCaptor.getValue().getStatus());
    verify(eventsNotificationService, times(1)).sendRideUpgradeAccepted(ride, request.getRequestedBy());
    assertTrue(result);
  }

  @Test
  public void testAcceptRequestReturnsFalseWhenRideIsRedispatched() throws Exception {
    when(riderService.getCurrentRider()).thenReturn(new RiderDto(RIDER_ID));
    when(repository.findByRiderAndStatus(anyLong(),
      eq(RideUpgradeRequestStatus.REQUESTED), eq(RideUpgradeRequestStatus.CANCELLED), eq(RideUpgradeRequestStatus.EXPIRED))).thenReturn(request);
    ride.getActiveDriver().getDriver().setId(DRIVER_ID + 100);
    when(rideDslRepository.findOne(ride.getId())).thenReturn(ride);

    boolean result = testedInstance.acceptRequest();

    assertFalse(result);
    verify(repository, never()).save(any(RideUpgradeRequest.class));
  }

  @Test
  public void testCancelRequestThrowsExceptionOnInactiveDriver() throws Exception {
    when(activeDriversService.getCurrentActiveDriver()).thenReturn(null);

    expected.expect(BadRequestException.class);
    expected.expectMessage("You can not cancel upgrade request while being offline");

    testedInstance.cancelRequest();
  }

  @Test
  public void testCancelRequestReturnsFalseIfRequestNotFound() throws Exception {
    when(activeDriversService.getCurrentActiveDriver()).thenReturn(activeDriver);
    when(repository.findByDriverAndStatus(DRIVER_ID, RideUpgradeRequestStatus.REQUESTED)).thenReturn(null);

    boolean result = testedInstance.cancelRequest();

    assertFalse(result);
    verify(repository, never()).save(any(RideUpgradeRequest.class));
  }

  @Test
  public void testCancelRequestReturnsTrueOnSuccessfulUpdate() throws Exception {
    when(activeDriversService.getCurrentActiveDriver()).thenReturn(activeDriver);
    when(repository.findByDriverAndStatus(DRIVER_ID, RideUpgradeRequestStatus.REQUESTED)).thenReturn(request);
    when(riderService.findRider(request.getRequestedFrom())).thenReturn(ride.getRider());

    boolean result = testedInstance.cancelRequest();

    assertTrue(result);
    verify(repository, times(1)).save(requestCaptor.capture());
    assertEquals(RideUpgradeRequestStatus.CANCELLED, requestCaptor.getValue().getStatus());
  }

  @Test
  public void testDeclineRequestReturnsFalseIfRequestNotFound() throws Exception {
    when(riderService.getCurrentRider()).thenReturn(new RiderDto(RIDER_ID));
    when(repository.findByRiderAndStatus(anyLong(), eq(RideUpgradeRequestStatus.REQUESTED))).thenReturn(null);

    boolean result = testedInstance.declineRequest();

    assertFalse(result);
    verify(repository, never()).save(any(RideUpgradeRequest.class));
  }

  @Test
  public void testDeclineRequestReturnsTrueOnSuccessfulUpdate() throws Exception {
    when(riderService.getCurrentRider()).thenReturn(new RiderDto(RIDER_ID));
    when(repository.findByRiderAndStatus(anyLong(), eq(RideUpgradeRequestStatus.REQUESTED), eq(RideUpgradeRequestStatus.EXPIRED))).thenReturn(request);

    boolean result = testedInstance.declineRequest();

    assertTrue(result);
    verify(repository, times(1)).save(requestCaptor.capture());
    assertEquals(RideUpgradeRequestStatus.DECLINED, requestCaptor.getValue().getStatus());
    verify(eventsNotificationService).sendRideUpgradeDeclined(request.getRideId(), request.getRequestedBy());
  }

  @Test
  public void testExpireRequests() throws Exception {
    when(repository.findExpired()).thenReturn(Collections.singletonList(request));
    when(riderService.findRider(request.getRequestedFrom())).thenReturn(ride.getRider());

    testedInstance.expireRequests();

    verify(repository, times(1)).saveMany(argThat(new TypeSafeMatcher<List<RideUpgradeRequest>>() {
      @Override
      protected boolean matchesSafely(List<RideUpgradeRequest> list) {
        return list.size() == 1 && list.get(0).getStatus() == RideUpgradeRequestStatus.EXPIRED;
      }

      @Override
      public void describeTo(Description description) {

      }
    }));
  }

  private void setupRequest(boolean alreadyExists) throws ForbiddenException {
    when(activeDriversService.getCurrentActiveDriver()).thenReturn(activeDriver);
    ride.setStatus(RideStatus.DRIVER_REACHED);
    ride.setRequestedCarType(CarType.builder().carCategory(SOURCE_CAR_CATEGORY).build());
    ride.setRiderSession(Session.builder().userAgent(RideUpgradeServiceTest.ELIGIBLE_RIDER_VERSION).build());
    when(rideDslRepository.findByDriverAndStatus(eq(activeDriver), anySetOf(RideStatus.class))).thenReturn(ride);
    when(configurationItemService.findByKeyAndCityId(anyString(), anyLong())).thenReturn(
      ConfigurationItem.builder()
        .configurationValue("{\"buttonTitle\":\"Ask rider to upgrade to SUV\",\"variants\":[{\"carCategory\":\"" + SOURCE_CAR_CATEGORY + "\",\"validUpgrades\":[\"" + CAR_CATEGORY + "\"]}]}")
        .build()
    );
    when(carTypesCache.getCarType(CAR_CATEGORY)).thenReturn(CarType.builder().carCategory(CAR_CATEGORY).build());
    when(repository.alreadyRequestedForRide(anyLong(), anyLong())).thenReturn(alreadyExists);

  }

  private void assertRequest(RideUpgradeRequest request) {
    assertEquals(CAR_CATEGORY, request.getTarget());
    assertEquals(SOURCE_CAR_CATEGORY, request.getSource());
    assertEquals(RideUpgradeRequestStatus.REQUESTED, request.getStatus());
    assertEquals(DRIVER_ID, request.getRequestedBy());
    assertEquals(RIDER_ID, request.getRequestedFrom());
    assertThat((double) request.getExpiresOn().getTime(), closeTo(Date.from(Instant.now().plus(EXPIRATION_TIMEOUT, ChronoUnit.SECONDS)).getTime(), 100.0));
  }

}