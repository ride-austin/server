package com.rideaustin.service;

import static com.google.common.collect.Lists.newArrayList;
import static com.rideaustin.service.ActiveDriversService.YOU_CANNOT_GET_OFFLINE_WHILE_RIDING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.concurrent.ThreadLocalRandom;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.maps.model.LatLng;
import com.rideaustin.model.City;
import com.rideaustin.model.TermsAcceptance;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.enums.CityApprovalStatus;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Car;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.ride.RideTracker;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.ActiveDriverDslRepository;
import com.rideaustin.repo.dsl.DriverDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.ForbiddenException;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.rest.exception.TermsNotAcceptedException;
import com.rideaustin.rest.model.CurrentActiveDriverDto;
import com.rideaustin.rest.model.PagingParams;
import com.rideaustin.service.config.GoOfflineConfig;
import com.rideaustin.service.config.StackedRidesConfig;
import com.rideaustin.service.eligibility.DriverEligibilityCheckService;
import com.rideaustin.service.event.EventsNotificationService;
import com.rideaustin.service.location.enums.LocationType;
import com.rideaustin.service.location.model.LocationObject;
import com.rideaustin.service.model.ActiveDriverInfo;
import com.rideaustin.service.model.ETCCalculationInfo;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.service.user.CarTypesCache;
import com.rideaustin.service.user.CarTypesUtils;
import com.rideaustin.service.user.DriverTypeCache;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

@RunWith(DataProviderRunner.class)
public class ActiveDriversServiceTest {

  private static long ID2 = 2L;
  private static long ID1 = 1L;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private ActiveDriversService testedInstance;

  @Mock
  private DriverDslRepository driverDslRepo;
  @Mock
  private ActiveDriverDslRepository activeDriverDslRepo;
  @Mock
  private RideDslRepository rideDslRepo;
  @Mock
  private CurrentUserService currentUserService;
  @Mock
  private CarTypesCache carTypesCache;
  @Mock
  private EventsNotificationService notificationService;
  @Mock
  private RideTrackerService rideTrackerService;
  @Mock
  private DriverEligibilityCheckService eligibilityCheckService;
  @Mock
  private CityService cityService;
  @Mock
  private TermsService termsService;
  @Mock
  private DriverTypeCache driverTypeCache;
  @Mock
  private ActiveDriverLocationService activeDriverLocationService;
  @Mock
  private MapService mapService;
  @Mock
  private RequestedDriversRegistry requestedDriversRegistry;
  @Mock
  private StackedDriverRegistry stackedDriverRegistry;
  @Mock
  private StackedRidesConfig stackedRidesConfig;
  @Mock
  private CityCache cityCache;
  @Mock
  private GoOfflineConfig goOfflineConfig;
  @Mock
  private User driverUser;

  @DataProvider
  public static Object[] deactivateReadyStatuses() {
    return EnumSet.of(ActiveDriverStatus.AVAILABLE, ActiveDriverStatus.AWAY, ActiveDriverStatus.REQUESTED).toArray();
  }

  @DataProvider
  public static Object[] ineligibleRideStackStatus() {
    return EnumSet.complementOf(EnumSet.of(RideStatus.ACTIVE)).toArray();
  }

  @Before
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);
    when(driverUser.isDriver()).thenReturn(true);
    when(currentUserService.getUser()).thenReturn(driverUser);

    testedInstance = new ActiveDriversService(driverDslRepo, activeDriverDslRepo, rideDslRepo, currentUserService,
      carTypesCache, notificationService, rideTrackerService, eligibilityCheckService, cityService, termsService,
      driverTypeCache, activeDriverLocationService, mapService, requestedDriversRegistry, stackedDriverRegistry,
      stackedRidesConfig, cityCache, goOfflineConfig);
  }

  @Test
  public void testGetCurrentActiveUserNoActiveDriver() throws Exception {
    when(activeDriverDslRepo.getActiveDrivers(anyObject())).thenReturn(Collections.emptyList());

    ActiveDriver activeDriver = testedInstance.getCurrentActiveDriver();

    assertThat(activeDriver, is(nullValue()));
  }

  @Test
  public void testGetCurrentActiveUserSingleActiveDriver() throws Exception {
    when(activeDriverDslRepo.getActiveDrivers(anyObject())).thenReturn(newArrayList(newActiveDriver(1L)));

    ActiveDriver activeDriver = testedInstance.getCurrentActiveDriver();

    assertThat(activeDriver, is(notNullValue()));
  }

  @Test
  public void testGetCurrentActiveUserMultipleActiveDrivers() throws Exception {
    // given
    when(activeDriverDslRepo.getActiveDrivers(anyObject())).thenReturn(newArrayList(newActiveDriver(ID2), newActiveDriver(ID1)));

    // when
    ActiveDriver activeDriver = testedInstance.getCurrentActiveDriver();

    // then
    assertThat(activeDriver, is(not(nullValue())));
    assertThat(activeDriver.getId(), is(ID2));
    verifyInactivatedDriver();
  }

  @Test
  public void testAdjustActiveDriverAvailableCarCategories() throws Exception {
    OnlineDriverDto mockedAD = newOnlineDriver(ID1);
    ;

    when(activeDriverLocationService.getById(anyLong(), anyObject())).thenReturn(mockedAD);

    Car car = new Car();
    car.setCarCategories(Sets.newHashSet("REGULAR"));
    car.setCarCategoriesBitmask(1);

    when(carTypesCache.toBitMask(anyObject())).thenReturn(1);

    testedInstance.adjustActiveDriverAvailableCarCategories(car, new Driver());
  }

  @Test
  public void testUpdateActiveDriverValues() throws Exception {
    // given
    ActiveDriver activeDriver = new ActiveDriver();
    activeDriver.setSelectedCar(new Car());
    activeDriver.setStatus(ActiveDriverStatus.AWAY);
    activeDriver.setCityId(1L);
    when(carTypesCache.fromBitMask(anyInt())).thenReturn(Collections.singleton("REGULAR"));

    ThreadLocalRandom random = ThreadLocalRandom.current();

    Double latitude = random.nextDouble();
    Double longitude = random.nextDouble();
    Double heading = random.nextDouble();
    Double speed = random.nextDouble();
    Double course = random.nextDouble();
    when(activeDriverDslRepo.findByIdWithDependencies(activeDriver.getId())).thenReturn(activeDriver);

    // when
    testedInstance.update(activeDriver.getId(), new ActiveDriverUpdateParams(latitude, longitude, heading,
      course, speed, Collections.emptySet(), Collections.emptySet(), 0L, 0L), 0L);

    // then
    verify(activeDriverDslRepo).save(activeDriver);
  }

  @Test
  public void updateAddsRideTracker() throws RideAustinException {
    final ActiveDriver activeDriver = new ActiveDriver();
    activeDriver.setStatus(ActiveDriverStatus.RIDING);
    activeDriver.setCityId(1L);
    when(activeDriverDslRepo.findByIdWithDependencies(anyLong())).thenReturn(activeDriver);

    testedInstance.update(1L, new ActiveDriverUpdateParams(34.68431, -97.19681, null,
      null, null, Collections.emptySet(), Collections.emptySet(), null, 1L), 1L);

    verify(rideTrackerService).updateRideTracker(eq(activeDriver), any(RideTracker.class));
  }

  @Test
  public void testDeactivateDriverNull() throws Exception {
    //given
    when(currentUserService.getUser()).thenReturn(null);

    // when
    testedInstance.deactivateAsAdmin(1L);

    // then
    verify(activeDriverDslRepo, never()).save(any(ActiveDriver.class));
  }

  @Test
  public void shouldNotDeactivateInactiveDriver() throws Exception {
    // given
    ActiveDriver activeDriver = newActiveDriver(ID1);
    activeDriver.setStatus(ActiveDriverStatus.INACTIVE);
    when(activeDriverDslRepo.findById(ID1)).thenReturn(activeDriver);

    // when
    testedInstance.deactivateAsAdmin(ID1);

    // then
    verify(activeDriverDslRepo, never()).save(any(ActiveDriver.class));
  }

  @Test
  @UseDataProvider("deactivateReadyStatuses")
  public void shouldDeactivateDriver(ActiveDriverStatus status) throws Exception {
    // given
    ActiveDriver activeDriver = newActiveDriver(ID1);
    activeDriver.setStatus(status);
    when(activeDriverDslRepo.findById(ID1)).thenReturn(activeDriver);

    // when
    testedInstance.deactivateAsAdmin(ID1);

    // then
    verifyInactivatedDriver();
  }

  @Test
  public void shouldNotDeactivateDriverInRidingStatus() throws Exception {
    // given
    ActiveDriver activeDriver = newActiveDriver(ID1);
    activeDriver.setStatus(ActiveDriverStatus.RIDING);
    expectedException.expect(BadRequestException.class);
    when(activeDriverDslRepo.findById(ID1)).thenReturn(activeDriver);
    when(rideDslRepo.findByActiveDriverAndStatuses(eq(activeDriver), eq(RideStatus.ONGOING_DRIVER_STATUSES))).thenReturn(Collections.singletonList(new Ride()));

    // when
    testedInstance.deactivateAsAdmin(ID1);

    // then
    expectedException.expectMessage(YOU_CANNOT_GET_OFFLINE_WHILE_RIDING);
  }

  @Test
  public void activateThrowsErrorWhenCityNotFound() throws RideAustinException {
    when(cityService.getById(anyLong())).thenReturn(null);

    expectedException.expect(NotFoundException.class);
    expectedException.expectMessage("City does not exist");

    testedInstance.activate(new ActiveDriverUpdateParams());
  }

  @Test
  public void activateThrowsErrorWhenTermsNotAccepted() throws RideAustinException {
    when(cityService.getById(anyLong())).thenReturn(new City());
    when(driverDslRepo.findByUser(driverUser)).thenReturn(new Driver());
    when(termsService.getDriverCurrentAcceptance(anyLong(), anyLong())).thenReturn(null);

    expectedException.expect(TermsNotAcceptedException.class);

    testedInstance.activate(new ActiveDriverUpdateParams());
  }

  @Test
  public void activateResolvesCarFromParams() throws RideAustinException {
    final long carId = 5L;
    final Driver driver = new Driver();
    final Car car = new Car();
    car.setSelected(true);
    car.setId(carId);
    driver.setCars(ImmutableSet.of(car));
    when(cityService.getById(anyLong())).thenReturn(new City());
    when(driverDslRepo.findByUser(driverUser)).thenReturn(driver);
    when(termsService.getDriverCurrentAcceptance(anyLong(), anyLong())).thenReturn(new TermsAcceptance());

    final ActiveDriverUpdateParams params = new ActiveDriverUpdateParams(34.189641681, -97.684618, null, null, null,
      Collections.singleton("REGULAR"), Collections.emptySet(), carId, 1L);

    testedInstance.activate(params);

    verify(activeDriverDslRepo).save(argThat(new ActiveDriverSelectedCarMatcher(car)));
  }

  @Test
  public void activateResolvesCarFromDriverProfile() throws RideAustinException {
    final long carId = 5L;
    final Driver driver = new Driver();
    final Car car = new Car();
    car.setSelected(true);
    car.setId(carId);
    driver.setCars(ImmutableSet.of(car));
    when(cityService.getById(anyLong())).thenReturn(new City());
    when(driverDslRepo.findByUser(driverUser)).thenReturn(driver);
    when(termsService.getDriverCurrentAcceptance(anyLong(), anyLong())).thenReturn(new TermsAcceptance());

    testedInstance.activate(new ActiveDriverUpdateParams(34.189641681, -97.684618, null, null, null,
      Collections.singleton("REGULAR"), Collections.emptySet(), null, 1L));

    verify(activeDriverDslRepo).save(argThat(new ActiveDriverSelectedCarMatcher(car)));
  }

  @Test
  public void updateLocationCallsUpdateWithNotEligibleForStackingWhenStackingIsDisabled() {
    final ActiveDriverUpdateParams params = new ActiveDriverUpdateParams(34.189641681, -97.684618, null, null, null,
      Collections.singleton("REGULAR"), Collections.emptySet(), null, 1L);
    final ActiveDriver activeDriver = new ActiveDriver();
    final Driver driver = new Driver();
    driver.setCityApprovalStatus(CityApprovalStatus.APPROVED);
    activeDriver.setDriver(driver);
    activeDriver.setCityId(1L);
    activeDriver.setSelectedCar(new Car());

    when(rideDslRepo.findActiveByActiveDriver(activeDriver)).thenReturn(new Ride());
    when(stackedRidesConfig.isStackingEnabled(anyLong())).thenReturn(false);

    testedInstance.updateLocation(activeDriver, params);

    verify(activeDriverLocationService).updateActiveDriverLocation(eq(params), any(ActiveDriverInfo.class), eq(false));
  }

  @Test
  public void updateLocationCallsUpdateWithNotEligibleForStackingWhenAlreadyStacked() {
    final ActiveDriverUpdateParams params = new ActiveDriverUpdateParams(34.189641681, -97.684618, null, null, null,
      Collections.singleton("REGULAR"), Collections.emptySet(), null, 1L);
    final ActiveDriver activeDriver = new ActiveDriver();
    final Driver driver = new Driver();
    driver.setCityApprovalStatus(CityApprovalStatus.APPROVED);
    activeDriver.setDriver(driver);
    activeDriver.setCityId(1L);
    activeDriver.setSelectedCar(new Car());

    when(rideDslRepo.findActiveByActiveDriver(activeDriver)).thenReturn(new Ride());
    when(stackedRidesConfig.isStackingEnabled(anyLong())).thenReturn(true);
    when(stackedDriverRegistry.isStacked(anyLong())).thenReturn(true);

    testedInstance.updateLocation(activeDriver, params);

    verify(activeDriverLocationService).updateActiveDriverLocation(eq(params), any(ActiveDriverInfo.class), eq(false));
  }

  @Test
  @UseDataProvider("ineligibleRideStackStatus")
  public void updateLocationCallsUpdateWithNotEligibleForStackingWhenRideIsNotActive(RideStatus status) {
    final ActiveDriverUpdateParams params = new ActiveDriverUpdateParams(34.189641681, -97.684618, null, null, null,
      Collections.singleton("REGULAR"), Collections.emptySet(), null, 1L);
    final ActiveDriver activeDriver = new ActiveDriver();
    final Driver driver = new Driver();
    driver.setCityApprovalStatus(CityApprovalStatus.APPROVED);
    activeDriver.setDriver(driver);
    activeDriver.setCityId(1L);
    activeDriver.setSelectedCar(new Car());

    when(rideDslRepo.findActiveByActiveDriver(activeDriver)).thenReturn(new Ride());
    when(stackedRidesConfig.isStackingEnabled(anyLong())).thenReturn(true);
    when(stackedDriverRegistry.isStacked(anyLong())).thenReturn(false);
    when(rideDslRepo.getETCCalculationInfo(anyLong())).thenReturn(new ETCCalculationInfo(1L, 1L, status, 34.0681381, -97.9841681));

    testedInstance.updateLocation(activeDriver, params);

    verify(activeDriverLocationService).updateActiveDriverLocation(eq(params), any(ActiveDriverInfo.class), eq(false));
  }

  @Test
  public void updateLocationCallsUpdateWithNotEligibleForStackingWhenRideDoesntHaveDestination() {
    final ActiveDriverUpdateParams params = new ActiveDriverUpdateParams(34.189641681, -97.684618, null, null, null,
      Collections.singleton("REGULAR"), Collections.emptySet(), null, 1L);
    final ActiveDriver activeDriver = new ActiveDriver();
    final Driver driver = new Driver();
    driver.setCityApprovalStatus(CityApprovalStatus.APPROVED);
    activeDriver.setDriver(driver);
    activeDriver.setCityId(1L);
    activeDriver.setSelectedCar(new Car());

    when(rideDslRepo.findActiveByActiveDriver(activeDriver)).thenReturn(new Ride());
    when(stackedRidesConfig.isStackingEnabled(anyLong())).thenReturn(true);
    when(stackedDriverRegistry.isStacked(anyLong())).thenReturn(false);
    when(rideDslRepo.getETCCalculationInfo(anyLong())).thenReturn(new ETCCalculationInfo(1L, 1L, RideStatus.ACTIVE, 0.0, 0.0));

    testedInstance.updateLocation(activeDriver, params);

    verify(activeDriverLocationService).updateActiveDriverLocation(eq(params), any(ActiveDriverInfo.class), eq(false));
  }

  @Test
  public void updateLocationCallsUpdateWithNotEligibleForStackingWhenDriverLocationIsMissing() {
    final ActiveDriverUpdateParams params = new ActiveDriverUpdateParams(34.189641681, -97.684618, null, null, null,
      Collections.singleton("REGULAR"), Collections.emptySet(), null, 1L);
    final ActiveDriver activeDriver = new ActiveDriver();
    final Driver driver = new Driver();
    driver.setCityApprovalStatus(CityApprovalStatus.APPROVED);
    activeDriver.setDriver(driver);
    activeDriver.setCityId(1L);
    activeDriver.setSelectedCar(new Car());

    when(rideDslRepo.findActiveByActiveDriver(activeDriver)).thenReturn(new Ride());
    when(stackedRidesConfig.isStackingEnabled(anyLong())).thenReturn(true);
    when(stackedDriverRegistry.isStacked(anyLong())).thenReturn(false);
    final ETCCalculationInfo etcCalculationInfo = new ETCCalculationInfo(1L, 1L, RideStatus.ACTIVE, 34.0, -97.0);
    etcCalculationInfo.setLocationObject(null);
    when(rideDslRepo.getETCCalculationInfo(anyLong())).thenReturn(etcCalculationInfo);

    testedInstance.updateLocation(activeDriver, params);

    verify(activeDriverLocationService).updateActiveDriverLocation(eq(params), any(ActiveDriverInfo.class), eq(false));
  }

  @Test
  public void updateLocationCallsUpdateWithEligibleForStackingWhenTTDLessThanThreshold() {
    final ActiveDriverUpdateParams params = new ActiveDriverUpdateParams(34.189641681, -97.684618, null, null, null,
      Collections.singleton("REGULAR"), Collections.emptySet(), null, 1L);
    final ActiveDriver activeDriver = new ActiveDriver();
    final Driver driver = new Driver();
    driver.setCityApprovalStatus(CityApprovalStatus.APPROVED);
    activeDriver.setDriver(driver);
    activeDriver.setCityId(1L);
    activeDriver.setSelectedCar(new Car());

    when(rideDslRepo.findActiveByActiveDriver(activeDriver)).thenReturn(new Ride());
    when(stackedRidesConfig.isStackingEnabled(anyLong())).thenReturn(true);
    when(stackedDriverRegistry.isStacked(anyLong())).thenReturn(false);
    final ETCCalculationInfo etcCalculationInfo = new ETCCalculationInfo(1L, 1L, RideStatus.ACTIVE, 34.0, -97.0);
    etcCalculationInfo.setLocationObject(new LocationObject());
    when(rideDslRepo.getETCCalculationInfo(anyLong())).thenReturn(etcCalculationInfo);
    when(mapService.getTimeToDriveCached(anyLong(), any(LatLng.class), any(LatLng.class))).thenReturn(10L);
    when(stackedRidesConfig.getEndRideTimeThreshold(anyLong())).thenReturn(60);

    testedInstance.updateLocation(activeDriver, params);

    verify(activeDriverLocationService).updateActiveDriverLocation(eq(params), any(ActiveDriverInfo.class), eq(true));
  }

  @Test
  public void disableCarCategoryInActiveDriverReturnsUpdatedCategories() {
    final long activeDriverId = 1L;
    final OnlineDriverDto driverDto = new OnlineDriverDto();
    driverDto.setAvailableCarCategoriesBitmask(7);
    when(activeDriverLocationService.getById(activeDriverId, LocationType.ACTIVE_DRIVER)).thenReturn(driverDto);

    final Integer result = testedInstance.disableCarCategoryInActiveDriver(activeDriverId, 2);

    assertEquals(5, result.intValue());
  }

  @Test
  public void getActiveDriversPageCollectsData() {
    testedInstance.getActiveDriversPage(1L, new PagingParams());
  }

  @Test
  public void getCurrentActiveDriverForDriverThrowsErrorWhenCurrentUserIsNotDriver() throws ForbiddenException {
    when(currentUserService.getUser()).thenReturn(new User());

    expectedException.expect(ForbiddenException.class);

    testedInstance.getCurrentActiveDriverForDriver();
  }

  @Test
  public void getCurrentActiveDriverForDriverDeactivatesOtherOnlineDrivers() throws ForbiddenException {
    final ActiveDriver activeDriver = new ActiveDriver();
    activeDriver.setId(1L);
    when(activeDriverDslRepo.getActiveDrivers(driverUser)).thenReturn(ImmutableList.of(
      activeDriver, new ActiveDriver()
    ));
    final OnlineDriverDto driverDto = new OnlineDriverDto();
    driverDto.setAvailableCarCategoriesBitmask(1);
    when(activeDriverLocationService.getById(activeDriver.getId(), LocationType.ACTIVE_DRIVER)).thenReturn(driverDto);

    final CurrentActiveDriverDto result = testedInstance.getCurrentActiveDriverForDriver();

    assertEquals(activeDriver.getId(), result.getId());
  }

  @Test
  public void getCurrentActiveDriverForDriverFindsFallback() throws ForbiddenException {
    final long driverId = 1L;
    final ActiveDriver activeDriver = new ActiveDriver();
    activeDriver.setId(driverId);
    when(activeDriverDslRepo.getActiveDrivers(driverUser)).thenReturn(Collections.emptyList());
    when(rideDslRepo.getOngoingRideDriverId(driverUser)).thenReturn(driverId);
    when(activeDriverDslRepo.findById(driverId)).thenReturn(activeDriver);
    final OnlineDriverDto driverDto = new OnlineDriverDto();
    driverDto.setAvailableCarCategoriesBitmask(1);
    when(activeDriverLocationService.getById(activeDriver.getId(), LocationType.ACTIVE_DRIVER)).thenReturn(driverDto);

    final CurrentActiveDriverDto result = testedInstance.getCurrentActiveDriverForDriver();

    assertEquals(activeDriver.getId(), result.getId());
  }

  @Test
  public void setAwayDriversInactiveDeactivatesDrivers() throws RideAustinException {
    final OnlineDriverDto driverDto = new OnlineDriverDto();
    final LocationObject locationObject = new LocationObject();
    locationObject.setLocationUpdateDate(Date.from(Instant.now().minus(10, ChronoUnit.SECONDS)));
    driverDto.setLocationObject(locationObject);
    when(activeDriverLocationService.getActiveDriversByStatus(ActiveDriverStatus.AWAY))
      .thenReturn(Collections.singletonList(driverDto));

    testedInstance.setAwayDriversInactive(new Date());

    verify(notificationService).sendGoOfflineToDriver(anyLong(), anyMapOf(String.class, String.class));
  }

  @Test
  public void setAvailableDriversInactiveWhenTermNotAccepted() throws RideAustinException {
    when(cityCache.getAllCities()).thenReturn(Collections.singletonList(new City()));
    final OnlineDriverDto driverDto = new OnlineDriverDto();
    driverDto.setStatus(ActiveDriverStatus.AVAILABLE);
    when(activeDriverLocationService.getAll()).thenReturn(Collections.singletonList(driverDto));
    when(termsService.getDriversCurrentAcceptance(any(), anyLong())).thenReturn(ImmutableMap.of(
      5L, new TermsAcceptance()
    ));

    testedInstance.setAvailableDriversInactiveWhenTermNotAccepted();

    verify(notificationService).sendGoOfflineToDriver(anyLong(), anyMapOf(String.class, String.class));
  }

  @Test
  public void setAvailableDriversAwayChangesStatus() {
    final OnlineDriverDto onlineDriverDto = new OnlineDriverDto();
    final LocationObject locationObject = new LocationObject();
    locationObject.setLocationUpdateDate(Date.from(Instant.now().minus(10, ChronoUnit.MINUTES)));
    onlineDriverDto.setLocationObject(locationObject);
    when(activeDriverLocationService.getActiveDriversByStatus(ActiveDriverStatus.AVAILABLE)).thenReturn(Collections.singletonList(
      onlineDriverDto
    ));

    testedInstance.setAvailableDriversAway(new Date());

    verify(activeDriverLocationService).updateActiveDriverLocationStatus(anyLong(), eq(ActiveDriverStatus.AWAY));
  }

  @Test
  public void adjustActiveDriverAvailableCarCategoriesDeactivatesDriver() throws ServerError {
    final Driver driver = new Driver();
    driver.setUser(driverUser);
    when(driverUser.getAvatars()).thenReturn(Collections.singletonList(driver));
    final ActiveDriver activeDriver = new ActiveDriver();
    activeDriver.setId(ID1);
    activeDriver.setDriver(driver);
    activeDriver.setStatus(ActiveDriverStatus.AVAILABLE);
    when(activeDriverDslRepo.getActiveDrivers(driverUser)).thenReturn(Collections.singletonList(activeDriver));
    final Car car = new Car();
    car.setCarCategoriesBitmask(5);
    final OnlineDriverDto onlineDriverDto = new OnlineDriverDto();
    onlineDriverDto.setAvailableCarCategoriesBitmask(2);
    when(activeDriverLocationService.getById(ID1, LocationType.ACTIVE_DRIVER)).thenReturn(onlineDriverDto);

    testedInstance.adjustActiveDriverAvailableCarCategories(car, driver);

    verify(notificationService).sendGoOfflineToDriver(anyLong(), anyMapOf(String.class, String.class));
  }

  private static class ActiveDriverSelectedCarMatcher extends BaseMatcher<ActiveDriver> {
    private final Car car;

    private ActiveDriverSelectedCarMatcher(Car car) {
      this.car = car;
    }

    @Override
    public boolean matches(Object o) {
      final ActiveDriver activeDriver = (ActiveDriver) o;
      return activeDriver.getSelectedCar().equals(car);
    }

    @Override
    public void describeTo(Description description) {

    }
  }

  private void verifyInactivatedDriver() {
    verify(activeDriverDslRepo).setInactive(eq(ID1));
  }

  private ActiveDriver newActiveDriver(Long id) {
    ActiveDriver ad = new ActiveDriver();
    ad.setId(id);
    return ad;
  }

  private OnlineDriverDto newOnlineDriver(Long id) {
    CarTypesUtils.setCarTypesCache(carTypesCache);
    Driver driver = new Driver();
    driver.setUser(new User());
    ActiveDriverInfo activeDriver = new ActiveDriverInfo(id, driver, new Car(), 1L);
    activeDriver.setAvailableCarCategoriesBitmask(1);
    return new OnlineDriverDto(activeDriver);
  }
}