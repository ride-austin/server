package com.rideaustin.service;

import static com.rideaustin.test.util.TestUtils.RANDOM;
import static com.rideaustin.test.util.TestUtils.mockCarType;
import static com.rideaustin.test.util.TestUtils.money;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import org.springframework.statemachine.StateMachinePersist;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.maps.model.LatLng;
import com.rideaustin.StubStateMachineContext;
import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.model.Address;
import com.rideaustin.model.Campaign;
import com.rideaustin.model.airports.Airport;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.promocodes.Promocode;
import com.rideaustin.model.promocodes.PromocodeRedemption;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.CityCarType;
import com.rideaustin.model.ride.FareDetails;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.ride.RideTracker;
import com.rideaustin.model.splitfare.FarePayment;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.ActiveDriverDslRepository;
import com.rideaustin.repo.dsl.PromocodeRedemptionDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.ForbiddenException;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.DispatcherAccountRideDto;
import com.rideaustin.rest.model.MobileDriverRideDto;
import com.rideaustin.rest.model.MobileRiderRideDto;
import com.rideaustin.rest.model.MobileRiderRideDto.PrecedingRide;
import com.rideaustin.rest.model.RideEndLocation;
import com.rideaustin.rest.model.RideStartLocation;
import com.rideaustin.service.airport.AirportService;
import com.rideaustin.service.config.RideJobServiceConfig;
import com.rideaustin.service.config.RidePaymentConfig;
import com.rideaustin.service.config.StackedRidesConfig;
import com.rideaustin.service.farepayment.FarePaymentService;
import com.rideaustin.service.location.enums.LocationType;
import com.rideaustin.service.location.model.LocationObject;
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.service.model.ETACalculationInfo;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.service.payment.PaymentEmailService;
import com.rideaustin.service.ride.RideOwnerService;
import com.rideaustin.service.user.CarTypesCache;
import com.rideaustin.service.user.CarTypesUtils;
import com.rideaustin.utils.map.LocationCorrector;
import com.rideaustin.utils.map.LocationCorrectorConfiguration.PickupHint.DesignatedPickup;

@RunWith(MockitoJUnitRunner.class)
public class RideServiceTest {

  private static final long ID = 1L;
  private static final String DATE1 = "2016-01-01";
  private static SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-mm-DD");
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private RideService testedInstance;
  @Mock
  private RidePaymentConfig rideServiceConfig;
  @Mock
  private RideJobServiceConfig rideJobServiceConfig;
  @Mock
  private RideDslRepository rideDslRepository;
  @Mock
  private RideTrackerService rideTrackerService;
  @Mock
  private FareService fareService;
  @Mock
  private MapService mapService;
  @Mock
  private CurrentUserService currentUserService;
  @Mock
  private RiderCardService riderCardService;
  @Mock
  private CarTypesCache carTypesCache;
  @Mock
  private AirportService airportService;
  @Mock
  private RiderLocationService riderLocationService;
  @Mock
  private ActiveDriverLocationService activeDriverLocationService;
  @Mock
  private RideOwnerService rideOwnerService;
  @Mock
  private ActiveDriverDslRepository activeDriverDslRepository;
  @Mock
  private StateMachinePersist<States, Events, String> contextAccess;
  @Mock
  private Environment environment;
  @Mock
  private StackedRidesConfig stackedRidesConfig;
  @Mock
  private StackedDriverRegistry stackedDriverRegistry;
  @Mock
  private CampaignService campaignService;
  @Mock
  private ConfigurationItemCache configurationItemCache;
  @Mock
  private PaymentEmailService paymentEmailService;
  @Mock
  private FarePaymentService farePaymentService;
  @Mock
  private PromocodeRedemptionDslRepository promocodeRedemptionDslRepository;
  @Mock
  private LocationCorrector locationCorrector;

  private Ride ride;
  private User riderUser;

  private User driverUser;

  @Before
  public void setup() throws Exception {
    Date date = dateFormat.parse(DATE1);

    riderUser = new User();
    riderUser.setId(RANDOM.nextLong());
    riderUser.setAvatarTypes(Sets.newHashSet(AvatarType.RIDER));
    Rider rider = new Rider();
    rider.setId(RANDOM.nextLong());
    rider.setUser(riderUser);

    driverUser = new User();
    Driver driver = new Driver();
    driver.setId(RANDOM.nextLong());
    driver.setUser(driverUser);
    ActiveDriver activeDriver = new ActiveDriver();
    activeDriver.setDriver(driver);
    driverUser.setAvatarTypes(Sets.newHashSet(AvatarType.DRIVER));

    ride = new Ride();
    ride.setId(RANDOM.nextLong());
    ride.setRider(rider);
    ride.setActiveDriver(activeDriver);
    ride.setDriverReachedOn(date);
    ride.setCreatedDate(date);
    ride.setDriverAcceptedOn(date);
    ride.setStatus(RideStatus.COMPLETED);

    when(rideDslRepository.findOne(anyLong())).thenReturn(ride);

    RideTracker rt = new RideTracker();
    rt.setDistanceTravelled(BigDecimal.TEN);
    when(rideTrackerService.endRide(anyLong(), any(RideTracker.class))).thenReturn(rt);
    when(rideDslRepository.save(any(Ride.class))).thenReturn(ride);
    when(currentUserService.getUser()).thenReturn(driverUser);
    when(carTypesCache.getCarType(anyString())).thenReturn(mockCarType());
    when(fareService.calculateTotalFare(eq(ride), any())).thenReturn(Optional.of(new FareDetails()));
    when(fareService.processCancellation(eq(ride), anyBoolean())).thenReturn(Optional.of(new FareDetails()));
    when(airportService.getAirportForLocation(anyDouble(), anyDouble())).thenReturn(Optional.of(new Airport()));

    testedInstance = new RideService(new ObjectMapper(), rideDslRepository, currentUserService, mapService, riderCardService,
      rideOwnerService, activeDriverLocationService, riderLocationService, campaignService,
      activeDriverDslRepository, paymentEmailService, farePaymentService, promocodeRedemptionDslRepository,
      locationCorrector, configurationItemCache, carTypesCache, stackedDriverRegistry, stackedRidesConfig,
      contextAccess, environment);
    when(rideServiceConfig.getTipLimit()).thenReturn(BigDecimal.valueOf(300d));
    when(rideJobServiceConfig.getRidePaymentDelay()).thenReturn(86400);
    when(rideServiceConfig.getCancellationChargeFreePeriod()).thenReturn(180);
  }

  @Test(expected = ForbiddenException.class)
  public void testGetRideAsRiderReturnNullIfNoRidersRide() throws Exception {
    testedInstance.getRideAsRider(1L, 0.0, 0.0);
  }

  @Test
  public void testGetRideAsRiderCallsRiderLocationServiceOnDriverAssignedRide() throws Exception {
    long rideId = 1L;
    double riderLat = 0.0;
    double riderLong = 0.0;
    setupGetRideAsRider(rideId, RideStatus.DRIVER_ASSIGNED);

    MobileRiderRideDto result = testedInstance.getRideAsRider(rideId, riderLat, riderLong);

    verify(riderLocationService, only()).processLocationUpdate(eq(ride.getRider().getId()), eq(ride.getActiveDriver().getDriver().getId()), eq(riderLat), eq(riderLong));
    assertEquals(rideId, result.getId());
  }

  @Test
  public void testGetRideAsRiderCallsRiderLocationServiceOnDriverReachedRide() throws Exception {
    long rideId = 1L;
    double riderLat = 0.0;
    double riderLong = 0.0;
    setupGetRideAsRider(rideId, RideStatus.DRIVER_REACHED);

    MobileRiderRideDto result = testedInstance.getRideAsRider(rideId, riderLat, riderLong);

    verify(riderLocationService, only()).processLocationUpdate(eq(ride.getRider().getId()), eq(ride.getActiveDriver().getDriver().getId()), eq(riderLat), eq(riderLong));
    assertEquals(rideId, result.getId());
  }

  @Test
  public void testGetRideAsRiderSkipsRiderLocationServiceOnRideNotInDriverAssignedOrDriverReached() throws Exception {
    long rideId = 1L;
    double riderLat = 0.0;
    double riderLong = 0.0;
    EnumSet<RideStatus> ineligibleRideStatuses = EnumSet.of(RideStatus.DRIVER_CANCELLED, RideStatus.RIDER_CANCELLED,
      RideStatus.ACTIVE, RideStatus.ADMIN_CANCELLED, RideStatus.COMPLETED, RideStatus.REQUESTED);
    when(campaignService.findMatchingCampaignForRide(anyLong())).thenReturn(Optional.empty());
    for (RideStatus status : ineligibleRideStatuses) {
      setupGetRideAsRider(rideId, status);

      MobileRiderRideDto result = testedInstance.getRideAsRider(rideId, riderLat, riderLong);

      verify(riderLocationService, never()).processLocationUpdate(eq(ride.getRider().getId()), eq(ride.getActiveDriver().getDriver().getId()), eq(riderLat), eq(riderLong));
      assertEquals(rideId, result.getId());
    }
  }

  @Test
  public void getCurrentRidesAsDispatcherReturnsDispatcherRideCollection() {
    final User user = new User();
    final Rider rider = new Rider();
    rider.setDispatcherAccount(true);
    user.addAvatar(rider);
    when(currentUserService.getUser()).thenReturn(user);

    testedInstance.getCurrentRidesAsDispatcher();

    verify(rideDslRepository).findOngoingRidesForDispatcher(user);
  }

  @Test
  public void getCurrentRidesAsDispatcherReturnsEmptyWhenRiderIsNotDispatcher() {
    final User user = new User();
    final Rider rider = new Rider();
    rider.setDispatcherAccount(false);
    user.addAvatar(rider);
    when(currentUserService.getUser()).thenReturn(user);

    final List<DispatcherAccountRideDto> result = testedInstance.getCurrentRidesAsDispatcher();

    assertTrue(result.isEmpty());
  }

  @Test
  public void getCurrentRideAsDriverReturnsNullWhenDriverNotFound() throws BadRequestException {
    final User user = new User();
    when(currentUserService.getUser()).thenReturn(user);
    when(activeDriverDslRepository.findByUserAndNotInactive(user)).thenReturn(null);

    final MobileDriverRideDto result = testedInstance.getCurrentRideAsDriver();

    assertNull(result);
  }

  @Test
  public void getCurrentRideAsDriverReturnsNullWhenDriverIsOffline() throws BadRequestException {
    final long activeDriverId = 1L;
    final User user = new User();
    final ActiveDriver activeDriver = new ActiveDriver();
    activeDriver.setId(activeDriverId);
    when(currentUserService.getUser()).thenReturn(user);
    when(activeDriverDslRepository.findByUserAndNotInactive(user)).thenReturn(activeDriver);
    when(activeDriverLocationService.getById(activeDriverId, LocationType.ACTIVE_DRIVER)).thenReturn(null);

    final MobileDriverRideDto result = testedInstance.getCurrentRideAsDriver();

    assertNull(result);
  }

  @Test
  public void getCurrentRideAsDriverReturnsNullWhenCurrentRideIsNullAndFallbackIsNull() throws BadRequestException {
    final long activeDriverId = 1L;
    final User user = new User();
    final ActiveDriver activeDriver = new ActiveDriver();
    activeDriver.setId(activeDriverId);
    final OnlineDriverDto onlineDriver = new OnlineDriverDto();
    onlineDriver.setStatus(ActiveDriverStatus.RIDING);
    when(currentUserService.getUser()).thenReturn(user);
    when(activeDriverDslRepository.findByUserAndNotInactive(user)).thenReturn(activeDriver);
    when(activeDriverLocationService.getById(activeDriverId, LocationType.ACTIVE_DRIVER)).thenReturn(onlineDriver);
    when(rideDslRepository.findCurrentForDriver(activeDriver)).thenReturn(Collections.emptyList());
    when(rideDslRepository.getRidesByStatusAndCreateDate(any(Date.class), anySetOf(RideStatus.class))).thenReturn(Collections.emptyList());

    final MobileDriverRideDto result = testedInstance.getCurrentRideAsDriver();

    assertNull(result);
  }

  @Test
  public void getCurrentRideAsDriverReturnsFallbackRide() throws Exception {
    final long activeDriverId = 1L;
    final User user = new User();
    final ActiveDriver activeDriver = new ActiveDriver();
    activeDriver.setId(activeDriverId);
    final OnlineDriverDto onlineDriver = new OnlineDriverDto();
    onlineDriver.setStatus(ActiveDriverStatus.RIDING);
    when(currentUserService.getUser()).thenReturn(user);
    when(activeDriverDslRepository.findByUserAndNotInactive(user)).thenReturn(activeDriver);
    when(activeDriverLocationService.getById(activeDriverId, LocationType.ACTIVE_DRIVER)).thenReturn(onlineDriver);
    when(rideDslRepository.findCurrentForDriver(activeDriver)).thenReturn(Collections.emptyList());
    final long fallbackRideId = 1L;
    final Ride fallbackRide = new Ride();
    fallbackRide.setId(fallbackRideId);
    when(rideDslRepository.getRidesByStatusAndCreateDate(any(Date.class), anySetOf(RideStatus.class))).thenReturn(Collections.singletonList(
      fallbackRide
    ));
    final MobileDriverRideDto expected = mock(MobileDriverRideDto.class);
    when(expected.getStatus()).thenReturn(RideStatus.DRIVER_ASSIGNED);

    final DispatchContext dispatchContext = new DispatchContext();
    final DispatchCandidate candidate = new DispatchCandidate();
    candidate.setId(activeDriverId);
    dispatchContext.setCandidate(candidate);
    dispatchContext.setId(fallbackRideId);
    when(contextAccess.read(anyString())).thenReturn(new StubStateMachineContext(
      ImmutableMap.of(
        "dispatchContext", dispatchContext
      )
    ));

    when(rideDslRepository.findOneForDriver(fallbackRideId)).thenReturn(expected);

    final MobileDriverRideDto result = testedInstance.getCurrentRideAsDriver();

    assertEquals(expected, result);
  }

  @Test
  public void getCurrentRideAsDriverReturnsActiveRideWhenAnotherIsAssigned() throws BadRequestException {
    final long activeDriverId = 1L;
    final User user = new User();
    final ActiveDriver activeDriver = new ActiveDriver();
    activeDriver.setId(activeDriverId);
    final OnlineDriverDto onlineDriver = new OnlineDriverDto();
    onlineDriver.setStatus(ActiveDriverStatus.RIDING);
    when(currentUserService.getUser()).thenReturn(user);
    when(activeDriverDslRepository.findByUserAndNotInactive(user)).thenReturn(activeDriver);
    when(activeDriverLocationService.getById(activeDriverId, LocationType.ACTIVE_DRIVER)).thenReturn(onlineDriver);
    final MobileDriverRideDto activeRide = mock(MobileDriverRideDto.class);
    when(activeRide.getStatus()).thenReturn(RideStatus.ACTIVE);
    final MobileDriverRideDto assignedRide = mock(MobileDriverRideDto.class);
    when(assignedRide.getStatus()).thenReturn(RideStatus.DRIVER_ASSIGNED);
    when(rideDslRepository.findCurrentForDriver(activeDriver)).thenReturn(ImmutableList.of(
      activeRide,
      assignedRide
    ));
    when(activeRide.getNextRide()).thenReturn(assignedRide);

    final MobileDriverRideDto result = testedInstance.getCurrentRideAsDriver();

    assertEquals(activeRide, result);
    verify(activeRide).setNextRide(assignedRide);
  }

  @Test
  public void getCurrentRideAsDriverReturnsActiveRideWhenAnotherIsReached() throws BadRequestException {
    final long activeDriverId = 1L;
    final User user = new User();
    final ActiveDriver activeDriver = new ActiveDriver();
    activeDriver.setId(activeDriverId);
    final OnlineDriverDto onlineDriver = new OnlineDriverDto();
    onlineDriver.setStatus(ActiveDriverStatus.RIDING);
    when(currentUserService.getUser()).thenReturn(user);
    when(activeDriverDslRepository.findByUserAndNotInactive(user)).thenReturn(activeDriver);
    when(activeDriverLocationService.getById(activeDriverId, LocationType.ACTIVE_DRIVER)).thenReturn(onlineDriver);
    final MobileDriverRideDto activeRide = mock(MobileDriverRideDto.class);
    when(activeRide.getStatus()).thenReturn(RideStatus.ACTIVE);
    final MobileDriverRideDto reachedRide = mock(MobileDriverRideDto.class);
    when(reachedRide.getStatus()).thenReturn(RideStatus.DRIVER_REACHED);
    when(rideDslRepository.findCurrentForDriver(activeDriver)).thenReturn(ImmutableList.of(
      activeRide,
      reachedRide
    ));
    when(activeRide.getNextRide()).thenReturn(reachedRide);

    final MobileDriverRideDto result = testedInstance.getCurrentRideAsDriver();

    assertEquals(activeRide, result);
    verify(activeRide).setNextRide(reachedRide);
  }

  @Test
  public void getCurrentRideAsDriverThrowsErrorWithTwoActiveRides() throws BadRequestException {
    final long activeDriverId = 1L;
    final User user = new User();
    final ActiveDriver activeDriver = new ActiveDriver();
    activeDriver.setId(activeDriverId);
    final OnlineDriverDto onlineDriver = new OnlineDriverDto();
    onlineDriver.setStatus(ActiveDriverStatus.RIDING);
    when(currentUserService.getUser()).thenReturn(user);
    when(activeDriverDslRepository.findByUserAndNotInactive(user)).thenReturn(activeDriver);
    when(activeDriverLocationService.getById(activeDriverId, LocationType.ACTIVE_DRIVER)).thenReturn(onlineDriver);
    final MobileDriverRideDto activeRide = mock(MobileDriverRideDto.class);
    when(activeRide.getStatus()).thenReturn(RideStatus.ACTIVE);
    final MobileDriverRideDto requestedRide = mock(MobileDriverRideDto.class);
    when(requestedRide.getStatus()).thenReturn(RideStatus.REQUESTED);
    when(rideDslRepository.findCurrentForDriver(activeDriver)).thenReturn(ImmutableList.of(
      activeRide,
      requestedRide
    ));

    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("You have more than one active ride. Please contact support");

    testedInstance.getCurrentRideAsDriver();
  }

  @Test
  public void getCurrentRideAsDriverThrowsErrorWithMoreThanTwoRides() throws BadRequestException {
    final long activeDriverId = 1L;
    final User user = new User();
    final ActiveDriver activeDriver = new ActiveDriver();
    activeDriver.setId(activeDriverId);
    final OnlineDriverDto onlineDriver = new OnlineDriverDto();
    onlineDriver.setStatus(ActiveDriverStatus.RIDING);
    when(currentUserService.getUser()).thenReturn(user);
    when(activeDriverDslRepository.findByUserAndNotInactive(user)).thenReturn(activeDriver);
    when(activeDriverLocationService.getById(activeDriverId, LocationType.ACTIVE_DRIVER)).thenReturn(onlineDriver);
    final MobileDriverRideDto activeRide = mock(MobileDriverRideDto.class);
    when(activeRide.getStatus()).thenReturn(RideStatus.ACTIVE);
    final MobileDriverRideDto requestedRide = mock(MobileDriverRideDto.class);
    when(requestedRide.getStatus()).thenReturn(RideStatus.REQUESTED);
    final MobileDriverRideDto assignedRide = mock(MobileDriverRideDto.class);
    when(assignedRide.getStatus()).thenReturn(RideStatus.DRIVER_ASSIGNED);
    when(rideDslRepository.findCurrentForDriver(activeDriver)).thenReturn(ImmutableList.of(
      activeRide,
      requestedRide,
      assignedRide
    ));

    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("You have more than two assigned rides. Please contact support");

    testedInstance.getCurrentRideAsDriver();
  }

  @Test
  public void getRideThrowsErrorWhenRideNotFound() throws NotFoundException {
    when(rideDslRepository.findOne(anyLong())).thenReturn(null);

    expectedException.expect(NotFoundException.class);
    expectedException.expectMessage("This ride does not exist");

    testedInstance.getRide(1L);
  }

  @Test
  public void getRideReturnsRide() throws NotFoundException {
    final Ride result = testedInstance.getRide(1L);

    assertEquals(ride, result);
  }

  @Test
  public void fillEndLocationSetsGooglePlaceAddress() throws RideAustinException {
    final RideEndLocation location = new RideEndLocation();
    location.setEndGooglePlaceId("123");

    final Address address = new Address();
    when(mapService.retrieveAddress(location.getGooglePlaceId())).thenReturn(address);

    testedInstance.fillEndLocation(ride, location, true);

    verify(mapService, never()).reverseGeocodeAddress(anyDouble(), anyDouble());
    assertEquals(address, ride.getEnd());
  }

  @Test
  public void fillEndLocationSetsGivenAddress() throws RideAustinException {
    final RideEndLocation location = new RideEndLocation();
    location.setEndAddress("Address");
    location.setEndZipCode("78701");

    testedInstance.fillEndLocation(ride, location, true);

    verify(mapService, never()).reverseGeocodeAddress(anyDouble(), anyDouble());
    verify(mapService, never()).retrieveAddress(anyString());
    assertEquals(location.getAddress(), ride.getEnd().getAddress());
    assertEquals(location.getZipCode(), ride.getEnd().getZipCode());
  }

  @Test
  public void fillEndLocationSetsReverseGeocodedAddress() throws RideAustinException {
    final RideEndLocation location = new RideEndLocation();
    location.setEndLocationLat(34.068131);
    location.setEndLocationLong(-97.68461);

    final Address address = new Address();
    when(mapService.reverseGeocodeAddress(location.getLat(), location.getLng())).thenReturn(address);
    when(locationCorrector.correctLocation(any(LatLng.class))).thenReturn(Optional.empty());

    testedInstance.fillEndLocation(ride, location, true);

    assertEquals(address, ride.getEnd());
    verify(mapService, never()).retrieveAddress(anyString());
  }

  @Test
  public void fillEndLocationSetsCorrectedReverseGeocodedAddress() throws RideAustinException {
    final RideEndLocation location = new RideEndLocation();
    location.setEndLocationLat(34.068131);
    location.setEndLocationLong(-97.68461);

    final Address address = new Address();
    address.setAddress("Address");
    when(mapService.reverseGeocodeAddress(location.getLat(), location.getLng())).thenReturn(address);
    final DesignatedPickup designatedPickup = new DesignatedPickup();
    designatedPickup.setName("AUX");
    when(locationCorrector.correctLocation(any(LatLng.class))).thenReturn(Optional.of(
      designatedPickup
    ));

    testedInstance.fillEndLocation(ride, location, true);

    assertEquals(address, ride.getEnd());
    assertEquals("Address (AUX)", ride.getEnd().getAddress());
    verify(mapService, never()).retrieveAddress(anyString());
  }

  @Test
  public void fillStartLocationSetsGooglePlaceAddress() throws RideAustinException {
    final RideStartLocation location = new RideStartLocation();
    location.setStartGooglePlaceId("123");

    final Address address = new Address();
    when(mapService.retrieveAddress(location.getGooglePlaceId())).thenReturn(address);

    testedInstance.fillStartLocation(ride, location);

    verify(mapService, never()).reverseGeocodeAddress(anyDouble(), anyDouble());
    assertEquals(address, ride.getStart());
  }

  @Test
  public void fillStartLocationSetsGivenAddress() throws RideAustinException {
    final RideStartLocation location = new RideStartLocation();
    location.setStartAddress("Address");
    location.setStartZipCode("78701");

    testedInstance.fillStartLocation(ride, location);

    verify(mapService, never()).reverseGeocodeAddress(anyDouble(), anyDouble());
    verify(mapService, never()).retrieveAddress(anyString());
    assertEquals(location.getAddress(), ride.getStart().getAddress());
    assertEquals(location.getZipCode(), ride.getStart().getZipCode());
  }

  @Test
  public void fillStartLocationSetsReverseGeocodedAddress() throws RideAustinException {
    final RideStartLocation location = new RideStartLocation();
    location.setStartLocationLat(34.068131);
    location.setStartLocationLong(-97.68461);

    final Address address = new Address();
    when(mapService.reverseGeocodeAddress(location.getLat(), location.getLng())).thenReturn(address);
    when(locationCorrector.correctLocation(any(LatLng.class))).thenReturn(Optional.empty());

    testedInstance.fillStartLocation(ride, location);

    assertEquals(address, ride.getStart());
    verify(mapService, never()).retrieveAddress(anyString());
  }

  @Test
  public void fillStartLocationSetsCorrectedReverseGeocodedAddress() throws RideAustinException {
    final RideStartLocation location = new RideStartLocation();
    location.setStartLocationLat(34.068131);
    location.setStartLocationLong(-97.68461);

    final Address address = new Address();
    address.setAddress("Address");
    when(mapService.reverseGeocodeAddress(location.getLat(), location.getLng())).thenReturn(address);
    final DesignatedPickup designatedPickup = new DesignatedPickup();
    designatedPickup.setName("AUX");
    when(locationCorrector.correctLocation(any(LatLng.class))).thenReturn(Optional.of(
      designatedPickup
    ));

    testedInstance.fillStartLocation(ride, location);

    assertEquals(address, ride.getStart());
    assertEquals("Address (AUX)", ride.getStart().getAddress());
    verify(mapService, never()).retrieveAddress(anyString());
  }

  @Test
  public void getRideAsDispatcherReturnsCampaignRide() throws ForbiddenException {
    final long rideId = 1L;
    when(rideOwnerService.isRideRider(rideId)).thenReturn(true);
    final DispatcherAccountRideDto rideDto = mock(DispatcherAccountRideDto.class);
    when(rideDto.getStatus()).thenReturn(RideStatus.COMPLETED);
    when(rideDto.getCompletedOn()).thenReturn(new Date());
    when(rideDto.getTotalCharge()).thenReturn(money(1.0));
    when(rideDslRepository.findDispatcherRideInfo(rideId)).thenReturn(rideDto);
    final Campaign campaign = new Campaign();
    campaign.setCappedAmount(money(0.5));
    when(campaignService.findMatchingCampaignForRide(rideId)).thenReturn(Optional.of(campaign));

    final DispatcherAccountRideDto result = testedInstance.getRideAsDispatcher(rideId);

    assertEquals(rideDto, result);
    verify(rideDto).setTotalFare(any(Money.class));
  }

  @Test
  public void getRideAsDispatcherThrowsErrorWhenRiderDoesntOwnRide() throws ForbiddenException {
    final long rideId = 1L;
    when(rideOwnerService.isRideRider(rideId)).thenReturn(false);

    expectedException.expect(ForbiddenException.class);
    expectedException.expectMessage("You can't access this ride");

    testedInstance.getRideAsDispatcher(rideId);
  }

  @Test
  public void getDrivingTimeToRiderReturnsFallbackValueWhenRideIsNotFound() {
    final long rideId = 1L;
    when(rideDslRepository.getETACalculationInfo(rideId)).thenReturn(null);

    final long result = testedInstance.getDrivingTimeToRider(rideId);

    assertEquals(RideService.FALLBACK_ETA, result);
  }

  @Test
  public void getDrivingTimeToRiderReturnsFallbackValueWhenDriverIsNotFound() {
    final long rideId = 1L;
    final ETACalculationInfo etaCalculationInfo = new ETACalculationInfo(1L, 34.8161684, -97.9841618);
    when(rideDslRepository.getETACalculationInfo(rideId)).thenReturn(etaCalculationInfo);
    when(activeDriverLocationService.getById(etaCalculationInfo.getActiveDriverId(), LocationType.ACTIVE_DRIVER)).thenReturn(null);

    final long result = testedInstance.getDrivingTimeToRider(rideId);

    assertEquals(RideService.FALLBACK_ETA, result);
  }

  @Test
  public void getDrivingTimeToRiderReturnsCachedETAWhenRideIsNotStacked() {
    final long rideId = 1L;
    final ETACalculationInfo etaCalculationInfo = new ETACalculationInfo(1L, 34.8161684, -97.9841618);
    when(rideDslRepository.getETACalculationInfo(rideId)).thenReturn(etaCalculationInfo);
    final OnlineDriverDto driverDto = new OnlineDriverDto();
    final LocationObject locationObject = new LocationObject();
    locationObject.setLatitude(34.681681);
    locationObject.setLongitude(-97.68161868);
    driverDto.setLocationObject(locationObject);
    when(activeDriverLocationService.getById(etaCalculationInfo.getActiveDriverId(), LocationType.ACTIVE_DRIVER)).thenReturn(driverDto);
    when(rideDslRepository.findPrecedingRide(rideId)).thenReturn(null);
    final long expected = 100L;
    when(mapService.getTimeToDriveCached(eq(rideId), any(LatLng.class), any(LatLng.class))).thenReturn(expected);

    final long result = testedInstance.getDrivingTimeToRider(rideId);

    assertEquals(expected, result);
  }

  @Test
  public void getDrivingTimeToRiderReturnsETCPlusETAPlusDropoff() {
    final long rideId = 1L;
    final ETACalculationInfo etaCalculationInfo = new ETACalculationInfo(1L, 34.8161684, -97.9841618);
    when(rideDslRepository.getETACalculationInfo(rideId)).thenReturn(etaCalculationInfo);
    final OnlineDriverDto driverDto = new OnlineDriverDto();
    final LocationObject locationObject = new LocationObject();
    locationObject.setLatitude(34.681681);
    locationObject.setLongitude(-97.68161868);
    driverDto.setLocationObject(locationObject);
    when(activeDriverLocationService.getById(etaCalculationInfo.getActiveDriverId(), LocationType.ACTIVE_DRIVER)).thenReturn(driverDto);
    when(rideDslRepository.findPrecedingRide(rideId)).thenReturn(new PrecedingRide(2L, RideStatus.ACTIVE, "", "", 34.68116, -97.9819816));
    when(stackedRidesConfig.getStackingDropoffExpectation()).thenReturn(80);
    when(mapService.getTimeToDriveCached(anyLong(), any(LatLng.class), any(LatLng.class))).thenReturn(null);

    final long result = testedInstance.getDrivingTimeToRider(rideId);

    assertEquals(200, result);
  }

  @Test
  public void resendReceiptLoadsPromocodeInfo() throws RideAustinException {
    final FarePayment farePayment = new FarePayment();
    ride.setPromocodeRedemptionId(1L);
    final PromocodeRedemption promocodeRedemption = new PromocodeRedemption();
    final Promocode promocode = new Promocode();
    promocodeRedemption.setPromocode(promocode);
    when(promocodeRedemptionDslRepository.findOne(ride.getPromocodeRedemptionId())).thenReturn(promocodeRedemption);
    when(farePaymentService.getFarePaymentForRide(ride)).thenReturn(farePayment);

    testedInstance.resendReceipt(ride.getId());

    verify(paymentEmailService).sendEndRideEmail(ride, farePayment, Collections.emptyList(), promocode);
  }

  private void setupGetRideAsRider(long rideId, RideStatus rideStatus) {
    User user = new User();
    Rider rider = new Rider();
    rider.setUser(user);
    user.addAvatar(rider);
    user.getAvatarTypes().add(AvatarType.RIDER);
    CarTypesUtils.setCarTypesCache(carTypesCache);
    Date completedOn = null;
    if (rideStatus == RideStatus.COMPLETED) {
      completedOn = new Date();
    }
    MobileRiderRideDto ride = new MobileRiderRideDto(ID, this.ride.getRider().getId(), rideStatus, null, completedOn,
      null, null, null, null, null, null, null,
      null, null, null, null, null, null, null,
      null, null, null, null, null, this.ride.getActiveDriver().getId(), this.ride.getActiveDriver().getDriver().getId(),
      5d, null, 1L, null, null, null, null,
      true, 1L, null, null, null, null, null, 1);
    when(rideDslRepository.findRiderRideInfo(eq(rideId))).thenReturn(ride);
    when(currentUserService.getUser()).thenReturn(user);
    when(rideOwnerService.isRideRider(rideId)).thenReturn(true);
  }

  public static Optional<CityCarType> mockCityCarType() {
    CityCarType ct = new CityCarType();
    ct.setCancellationFee(Money.of(CurrencyUnit.USD, 5.00d));
    return Optional.of(ct);
  }

}
