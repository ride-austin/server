package com.rideaustin.service;

import static com.rideaustin.utils.SafeZeroUtils.safeZero;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.persistence.LockTimeoutException;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.maps.model.LatLng;
import com.rideaustin.model.City;
import com.rideaustin.model.TermsAcceptance;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.enums.CityApprovalStatus;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Car;
import com.rideaustin.model.ride.DriverType;
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
import com.rideaustin.rest.model.ActiveDriverDto;
import com.rideaustin.rest.model.CurrentActiveDriverDto;
import com.rideaustin.rest.model.PagingParams;
import com.rideaustin.service.config.GoOfflineConfig;
import com.rideaustin.service.config.StackedRidesConfig;
import com.rideaustin.service.eligibility.DriverEligibilityCheckContext;
import com.rideaustin.service.eligibility.DriverEligibilityCheckService;
import com.rideaustin.service.eligibility.checks.CarCategoryEligibilityCheck;
import com.rideaustin.service.eligibility.checks.DriverTypeEligibilityCheck;
import com.rideaustin.service.event.EventsNotificationService;
import com.rideaustin.service.location.enums.LocationType;
import com.rideaustin.service.model.ActiveDriverInfo;
import com.rideaustin.service.model.ETCCalculationInfo;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.service.user.CarTypesCache;
import com.rideaustin.service.user.DriverTypeCache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ActiveDriversService {

  protected static final String YOU_CANNOT_GET_OFFLINE_WHILE_RIDING = "You cannot get offline while in an active ride";
  private static final String SOURCE = "source";

  private final DriverDslRepository driverDslRepo;
  private final ActiveDriverDslRepository activeDriverDslRepo;
  private final RideDslRepository rideDslRepo;
  private final CurrentUserService currentUserService;
  private final CarTypesCache carTypesCache;
  private final EventsNotificationService notificationService;
  private final RideTrackerService rideTrackerService;
  private final DriverEligibilityCheckService eligibilityCheckService;
  private final CityService cityService;
  private final TermsService termsService;
  private final DriverTypeCache driverTypeCache;
  private final ActiveDriverLocationService activeDriverLocationService;
  private final MapService mapService;
  private final RequestedDriversRegistry requestedDriversRegistry;
  private final StackedDriverRegistry stackedDriverRegistry;

  private final StackedRidesConfig stackedRidesConfig;
  private final CityCache cityCache;
  private final GoOfflineConfig goOfflineConfig;

  public enum GoOfflineEventSource {
    DRIVER_INACTIVE, MISSED_RIDES, CAR_TYPES_DEACTIVATE, ADMIN_DISABLE, TERMS_NOT_ACCEPTED
  }

  @Transactional
  public LatLng activate(ActiveDriverUpdateParams params) throws RideAustinException {

    User user = currentUserService.getUser();
    Driver driver = driverDslRepo.findByUser(user);

    City city = cityService.getById(params.getCityId());
    if (city == null) {
      throw new NotFoundException("City does not exist");
    }

    if (termsService.getDriverCurrentAcceptance(driver.getId(), driver.getCityId()) == null) {
      throw new TermsNotAcceptedException();
    }

    Optional<Car> car = Optional.empty();
    if (params.getCarId() != null) {
      car = driver.getCars()
        .stream()
        .filter(Car::isSelected)
        .filter(c -> c.getId() == params.getCarId())
        .findFirst();
    } else if (!driver.getCars().isEmpty()) {
      car = driver.getCars().stream().filter(Car::isSelected).findFirst();
    }

    ActiveDriver activeDriver = getCurrentActiveDriver();
    if (activeDriver == null) {
      activeDriver = new ActiveDriver();
      activeDriver.setDriver(driver);
    }

    if (activeDriver.getStatus() == null) {
      activeDriver.setStatus(ActiveDriverStatus.AVAILABLE);
    }
    activeDriver.setSelectedCar(car.orElse(null));
    activeDriver.setCityId(params.getCityId());

    eligibilityCheckService.check(
      new DriverEligibilityCheckContext(
        driver, activeDriver, car.orElse(null),
        ImmutableMap.of(
          DriverEligibilityCheckContext.CAR_CATEGORIES, params.getCarCategories(),
          DriverEligibilityCheckContext.DRIVER_TYPES, params.getDriverTypes(),
          DriverEligibilityCheckContext.CITY, params.getCityId()
        )),
      params.getCityId()
    );

    activeDriverDslRepo.save(activeDriver);

    final int availableCarCategoriesBitmask = carTypesCache.toBitMask(params.getCarCategories());
    final int carCategories = car.map(Car::getCarCategoriesBitmask).orElse(0);
    if (availableCarCategoriesBitmask > carCategories) {
      log.info(String.format("[RA15196][AD %d] Car has %d bitmask, params are %d", activeDriver.getId(),
        carCategories, availableCarCategoriesBitmask));
    }
    ActiveDriverInfo activeDriverInfo = new ActiveDriverInfo(activeDriver.getId(),
      availableCarCategoriesBitmask, safeZero(driverTypeCache.toBitMask(params.getDriverTypes())),
      params.getCityId(), driver, car.orElse(null), activeDriver.getStatus());

    activeDriverLocationService.updateActiveDriverLocation(params, activeDriverInfo, false);
    return new LatLng(params.getLatitude(), params.getLongitude());
  }

  @Transactional
  public ActiveDriver update(long activeDriverId, ActiveDriverUpdateParams params, Long sequence) throws RideAustinException {
    boolean needActiveDriverUpdate = false;

    //reload active driver object within transaction to avoid lazy loading exceptions in eligibility check
    ActiveDriver activeDriver = activeDriverDslRepo.findByIdWithDependencies(activeDriverId);

    eligibilityCheckService.check(
      new DriverEligibilityCheckContext(
        null, activeDriver, activeDriver.getSelectedCar(),
        ImmutableMap.<String, Object>builder()
          .put(DriverEligibilityCheckContext.CAR_CATEGORIES, params.getCarCategories())
          .put(DriverEligibilityCheckContext.DRIVER_TYPES, params.getDriverTypes())
          .put(DriverEligibilityCheckContext.CITY, activeDriver.getCityId())
          .build(),
        ImmutableSet.of(CarCategoryEligibilityCheck.class, DriverTypeEligibilityCheck.class)
      ),
      params.getCityId()
    );

    ActiveDriverStatus status = activeDriver.getStatus();
    if (status == ActiveDriverStatus.RIDING) {
      RideTracker rideTracker = new RideTracker(params.getLatitude(), params.getLongitude(),
        params.getSpeed(), params.getHeading(), params.getCourse(), sequence);
      rideTrackerService.updateRideTracker(activeDriver, rideTracker);
    } else if (status == ActiveDriverStatus.AWAY) {
      activeDriver.setStatus(ActiveDriverStatus.AVAILABLE);
      needActiveDriverUpdate = true;
    }
    if (needActiveDriverUpdate) {
      activeDriverDslRepo.save(activeDriver);
    }

    return activeDriver;
  }

  public LatLng updateLocation(ActiveDriver activeDriver, ActiveDriverUpdateParams params) {
    boolean eligibleForStacking = false;
    Ride ride = rideDslRepo.findActiveByActiveDriver(activeDriver);
    if (ride != null) {
      eligibleForStacking = isEligibleForStacking(activeDriver, ride.getId(), activeDriver.getCityId());
    }

    final int availableCarCategoriesBitmask = carTypesCache.toBitMask(params.getCarCategories());
    final Car selectedCar = activeDriver.getSelectedCar();
    final int carCategories = selectedCar.getCarCategoriesBitmask();
    if (availableCarCategoriesBitmask > carCategories) {
      log.info(String.format("[RA15196][AD %d] Car has %d bitmask, params are %d", activeDriver.getId(),
        carCategories, availableCarCategoriesBitmask));
    }

    Integer availableDriverTypesBitmask = safeZero(driverTypeCache.toBitMask(params.getDriverTypes()));
    if (CityApprovalStatus.APPROVED.equals(activeDriver.getDriver().getCityApprovalStatus())) {
      availableDriverTypesBitmask |= driverTypeCache.toBitMask(Collections.singleton(DriverType.FINGERPRINTED));
    }
    ActiveDriverInfo activeDriverInfo = new ActiveDriverInfo(activeDriver.getId(), carTypesCache.toBitMask(params.getCarCategories()),
      availableDriverTypesBitmask, activeDriver.getCityId(), activeDriver.getDriver(), selectedCar,
      activeDriver.getStatus());

    activeDriverLocationService.updateActiveDriverLocation(params, activeDriverInfo, eligibleForStacking);
    return new LatLng(params.getLatitude(), params.getLongitude());
  }

  public Integer disableCarCategoryInActiveDriver(Long activeDriverId, Integer carCategoryToDisable) {
    OnlineDriverDto activeDriver = activeDriverLocationService.getById(activeDriverId, LocationType.ACTIVE_DRIVER);
    Integer updatedCarCategories = activeDriver.getAvailableCarCategoriesBitmask() ^ carCategoryToDisable;
    updateActiveDriverCarCategories(activeDriverId, updatedCarCategories);
    return updatedCarCategories;
  }

  public List<OnlineDriverDto> getActiveDrivers() {
    return activeDriverLocationService.getAll()
      .stream()
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  public Page<ActiveDriverDto> getActiveDriversPage(Long cityId, PagingParams paging) {
    Predicate<OnlineDriverDto> predicate = ad -> ad != null && (cityId == null || cityId.equals(ad.getCityId()));
    List<OnlineDriverDto> availableDrivers = activeDriverLocationService.getActiveDriversByStatus(ActiveDriverStatus.AVAILABLE)
      .stream()
      .filter(predicate)
      .collect(Collectors.toList());
    List<OnlineDriverDto> awayDrivers = activeDriverLocationService.getActiveDriversByStatus(ActiveDriverStatus.AWAY)
      .stream()
      .filter(predicate)
      .collect(Collectors.toList());

    List<OnlineDriverDto> drivers = new ArrayList<>();
    drivers.addAll(availableDrivers);
    drivers.addAll(awayDrivers);
    drivers.sort(Comparator.comparing(OnlineDriverDto::getId));

    List<ActiveDriverDto> content = drivers.subList(paging.getPage() * paging.getPageSize(),
      Math.min(paging.getPageSize(), drivers.size()))
      .stream()
      .map(o -> new ActiveDriverDto(o.getLatitude(), o.getLongitude(), o.getAvailableCarCategoriesBitmask(), o.getDriverId(),
        o.getFullName(), o.getPhoneNumber(), o.getUserId()))
      .collect(Collectors.toList());

    return new PageImpl<>(content, paging.toPageRequest(), content.size());
  }

  @Transactional
  public ActiveDriver getCurrentActiveDriver() throws ForbiddenException {
    User currentUser = currentUserService.getUser();
    if (!currentUser.isDriver()) {
      throw new ForbiddenException();
    }
    ActiveDriver activeDriver = getActiveDriverByDriver(currentUser);
    if (activeDriver == null) {
      Ride ongoingRide = rideDslRepo.getOngoingRideForDriver(currentUser);
      if (ongoingRide != null) {
        activeDriver = activeDriverDslRepo.findById(ongoingRide.getActiveDriver().getId());
        activeDriver.setStatus(ActiveDriverStatus.RIDING);
      }
    }
    return activeDriver;
  }

  public CurrentActiveDriverDto getCurrentActiveDriverForDriver() throws ForbiddenException {
    User currentUser = currentUserService.getUser();
    if (!currentUser.isDriver()) {
      throw new ForbiddenException();
    }
    List<ActiveDriver> activeDrivers = activeDriverDslRepo.getActiveDrivers(currentUser);
    ActiveDriver activeDriver = null;
    if (!activeDrivers.isEmpty()) {
      if (activeDrivers.size() > 1) {
        deactivateDrivers(activeDrivers.subList(1, activeDrivers.size()));
      }
      activeDriver = activeDrivers.get(0);
    }
    if (activeDriver == null) {
      Long driverId = rideDslRepo.getOngoingRideDriverId(currentUser);
      if (driverId != null) {
        activeDriver = activeDriverDslRepo.findById(driverId);
        activeDriver.setStatus(ActiveDriverStatus.RIDING);
      }
    }
    if (activeDriver != null) {
      OnlineDriverDto onlineDriver = activeDriverLocationService.getById(activeDriver.getId(), LocationType.ACTIVE_DRIVER);
      if (onlineDriver != null) {
        return new CurrentActiveDriverDto(onlineDriver.getId(), onlineDriver.getDriverId(), onlineDriver.getUserId(),
          onlineDriver.getStatus(), onlineDriver.getAvailableCarCategoriesBitmask());
      }
    }
    return null;
  }

  public ActiveDriver getActiveDriverByDriver(User user) {
    List<ActiveDriver> activeDrivers = activeDriverDslRepo.getActiveDrivers(user);
    if (!activeDrivers.isEmpty()) {
      if (activeDrivers.size() > 1) {
        deactivateDrivers(activeDrivers.subList(1, activeDrivers.size()));
      }
      if (activeDrivers.get(0).getDriver() != null
        && activeDrivers.get(0).getDriver().getUser() != null
        && activeDrivers.get(0).getDriver().getUser().getAvatars() != null) {
        log.debug("Loaded avatars fr AD: {}", activeDrivers.get(0).getDriver().getUser().getAvatars().size());
      }
      return activeDrivers.get(0);
    }
    return null;
  }

  @Transactional(noRollbackFor = {LockTimeoutException.class, LockAcquisitionException.class, CannotAcquireLockException.class, SQLException.class})
  public void setAwayDriversInactive(Date locationUpdatedInactiveInterval) throws RideAustinException {
    List<OnlineDriverDto> activeDrivers = activeDriverLocationService.getActiveDriversByStatus(ActiveDriverStatus.AWAY);
    if (activeDrivers != null) {
      activeDrivers = activeDrivers
        .stream()
        .filter(ad -> ad.getLocationUpdatedOn().before(locationUpdatedInactiveInterval))
        .collect(Collectors.toList());
    }

    for (OnlineDriverDto driverForUpdate : activeDrivers) {
      deactivateWithMessage(driverForUpdate, GoOfflineEventSource.DRIVER_INACTIVE);
      log.info("Setting active driver {} to {}", driverForUpdate.getId(), ActiveDriverStatus.INACTIVE);
    }
  }

  @Transactional(noRollbackFor = {LockTimeoutException.class, LockAcquisitionException.class, CannotAcquireLockException.class, SQLException.class})
  public void setAvailableDriversInactiveWhenTermNotAccepted() throws RideAustinException {
    List<City> cities = cityCache.getAllCities();

    List<OnlineDriverDto> activeDrivers = activeDriverLocationService.getAll();
    for (City city : cities) {
      List<Long> driversIds = activeDrivers.stream().map(OnlineDriverDto::getDriverId).collect(Collectors.toList());
      Map<Long, TermsAcceptance> acceptanceMap = termsService.getDriversCurrentAcceptance(driversIds, city.getId());

      activeDrivers = activeDrivers
        .stream()
        .filter(ad -> ActiveDriverStatus.AVAILABLE.equals(ad.getStatus())
          && (!acceptanceMap.containsKey(ad.getDriverId())))
        .collect(Collectors.toList());

      for (OnlineDriverDto driverForUpdate : activeDrivers) {
        deactivateWithMessage(driverForUpdate, GoOfflineEventSource.TERMS_NOT_ACCEPTED);
        log.info("Setting active driver {} to {} due to not accepted terms", driverForUpdate.getId(), ActiveDriverStatus.INACTIVE);
      }
    }
  }

  @Transactional(noRollbackFor = {LockTimeoutException.class, LockAcquisitionException.class, CannotAcquireLockException.class, SQLException.class})
  public void setAvailableDriversAway(Date locationUpdatedAwayLimit) {
    List<OnlineDriverDto> activeDrivers = activeDriverLocationService.getActiveDriversByStatus(ActiveDriverStatus.AVAILABLE);
    if (activeDrivers != null) {
      activeDrivers = activeDrivers.stream()
        .filter(ad -> ad.getLocationUpdatedOn().before(locationUpdatedAwayLimit))
        .collect(Collectors.toList());
    } else {
      activeDrivers = Collections.emptyList();
    }

    for (OnlineDriverDto driverForUpdate : activeDrivers) {
      activeDriverDslRepo.setAway(driverForUpdate.getId());
      activeDriverLocationService.updateActiveDriverLocationStatus(driverForUpdate.getId(), ActiveDriverStatus.AWAY);
      log.info("Setting active driver {} to {}", driverForUpdate.getId(), ActiveDriverStatus.AWAY);
    }
  }

  /**
   * Adjusting current ActiveDriver availableCarCategories to suits current car carCategories
   * Disable non-riding ActiveDriver if last carCategories from available was removed.
   *
   * @param car
   * @param driver
   * @throws BadRequestException
   */
  public void adjustActiveDriverAvailableCarCategories(Car car, Driver driver) throws ServerError {
    ActiveDriver activeDriver = getActiveDriverByDriver(driver.getUser());
    if (null != activeDriver) {
      OnlineDriverDto onlineDriver = activeDriverLocationService.getById(activeDriver.getId(), LocationType.ACTIVE_DRIVER);
      if (onlineDriver.getAvailableCarCategoriesBitmask() != 0 && car.getCarCategoriesBitmask() != 0) {
        Integer onlyAvailableCarCategoriesBitmask = onlineDriver.getAvailableCarCategoriesBitmask() & car.getCarCategoriesBitmask();
        if (!onlyAvailableCarCategoriesBitmask.equals(onlineDriver.getAvailableCarCategoriesBitmask())) {
          updateActiveDriverCarCategories(activeDriver, onlyAvailableCarCategoriesBitmask);
        }
      }
    }
  }

  @Transactional
  public void deactivateAsDriver() throws BadRequestException {
    User user = currentUserService.getUser();
    ActiveDriver activeDriver = activeDriverDslRepo.findByUserAndNotInactive(user);
    if (!user.isDriver() || activeDriver == null) {
      return;
    }
    deactivateAsAdmin(activeDriver.getId());
  }

  @Transactional
  public void deactivateAsAdmin(long activeDriverId) throws BadRequestException {
    ActiveDriver activeDriver = activeDriverDslRepo.findById(activeDriverId);
    if (activeDriver == null) {
      return;
    }
    ActiveDriverStatus status = Optional.ofNullable(activeDriverLocationService.getById(activeDriver.getId(), LocationType.ACTIVE_DRIVER))
      .map(OnlineDriverDto::getStatus)
      .orElse(activeDriver.getStatus());
    if (ActiveDriverStatus.RIDING.equals(activeDriver.getStatus()) && ActiveDriverStatus.RIDING.equals(status)) {
      if (CollectionUtils.isEmpty(rideDslRepo.findByActiveDriverAndStatuses(activeDriver, RideStatus.ONGOING_DRIVER_STATUSES))) {
        activeDriverDslRepo.setRidingDriverAsAvailable(activeDriverId);
        deactivate(activeDriver);
        return;
      }
      throw new BadRequestException(YOU_CANNOT_GET_OFFLINE_WHILE_RIDING);
    }
    if (!ActiveDriverStatus.INACTIVE.equals(status)) {
      deactivate(activeDriver);
    }
  }

  public void deactivateWithMessage(ActiveDriver activeDriver, GoOfflineEventSource source) throws ServerError {
    deactivate(activeDriver);
    String message = goOfflineConfig.getGoOfflineMessage(source);
    ImmutableMap.Builder<String, String> builder = createNotificationParameters(source, message);
    notificationService.sendGoOfflineToDriver(activeDriver.getDriver().getId(), builder.build());
  }

  private ImmutableMap.Builder<String, String> createNotificationParameters(GoOfflineEventSource source, String message) {
    ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
    builder.put(SOURCE, source.toString());
    if (message != null) {
      builder.put("message", message);
    }
    return builder;
  }

  private void deactivateWithMessage(OnlineDriverDto activeDriver, GoOfflineEventSource source) throws ServerError {
    deactivate(activeDriver);
    String message = goOfflineConfig.getGoOfflineMessage(source);
    ImmutableMap.Builder<String, String> builder = createNotificationParameters(source, message);
    notificationService.sendGoOfflineToDriver(activeDriver.getDriverId(), builder.build());
  }

  public void deactivateWithMessage(Long activeDriverId, Long driverId, GoOfflineEventSource source) throws ServerError {
    deactivate(activeDriverDslRepo.findById(activeDriverId));
    String message = goOfflineConfig.getGoOfflineMessage(source);
    Map<String, String> parameters;
    if (message != null) {
      parameters = ImmutableMap.of(SOURCE, source.toString(), "message", message);
    } else {
      parameters = ImmutableMap.of(SOURCE, source.toString());
    }
    notificationService.sendGoOfflineToDriver(driverId, parameters);
  }

  private void deactivateDrivers(List<ActiveDriver> activeDrivers) {
    for (ActiveDriver activeDriver : activeDrivers) {
      deactivate(activeDriver);
    }
  }

  private void deactivate(OnlineDriverDto activeDriver) {
    activeDriverDslRepo.setInactive(activeDriver.getId());
    activeDriverLocationService.removeLocationObject(activeDriver.getId());
    if (requestedDriversRegistry.isRequested(activeDriver.getId())) {
      requestedDriversRegistry.remove(activeDriver.getId());
    }
  }

  private void deactivate(ActiveDriver activeDriver) {
    activeDriverDslRepo.setInactive(activeDriver.getId());
    activeDriverLocationService.removeLocationObject(activeDriver.getId());
    requestedDriversRegistry.remove(activeDriver.getId());
  }

  private void updateActiveDriverCarCategories(Long activeDriverId, Integer updatesCarCategories) {
    OnlineDriverDto activeDriver = activeDriverLocationService.getById(activeDriverId, LocationType.ACTIVE_DRIVER);
    activeDriver.setAvailableCarCategoriesBitmask(updatesCarCategories);
    activeDriverLocationService.saveOrUpdateLocationObject(activeDriver);
  }

  private void updateActiveDriverCarCategories(ActiveDriver activeDriver, Integer updatedCarCategories) throws ServerError {
    if (updatedCarCategories == 0 && ActiveDriverStatus.AVAILABLE.equals(activeDriver.getStatus())) {
      deactivateWithMessage(activeDriver, GoOfflineEventSource.CAR_TYPES_DEACTIVATE);
    } else {
      updateActiveDriverCarCategories(activeDriver.getId(), updatedCarCategories);
    }
  }

  private boolean isEligibleForStacking(ActiveDriver activeDriver, Long rideId, Long cityId) {
    if (!stackedRidesConfig.isStackingEnabled(cityId)) {
      log.info(String.format("AD %d: Stacked rides are disabled, skipping", activeDriver.getId()));
      return false;
    }
    //    if driver already has stacked rides, skip the driver
    if (stackedDriverRegistry.isStacked(activeDriver.getId())) {
      log.info(String.format("AD %d: Is already stacked, skipping", activeDriver.getId()));
      return false;
    }
    ETCCalculationInfo etcCalculationInfo = rideDslRepo.getETCCalculationInfo(rideId);
    if (etcCalculationInfo.getStatus() != RideStatus.ACTIVE) {
      //should never happen but if driver is close enough to destination at ride accepting, skip them
      log.info(String.format("AD %d: Is not in an ACTIVE ride, skipping", activeDriver.getId()));
      return false;
    }
    //    if ride doesn't have destination set, skip the driver
    if (Double.compare(etcCalculationInfo.getEndLat(), 0.0) == 0 || Double.compare(etcCalculationInfo.getEndLng(), 0.0) == 0) {
      log.info(String.format("AD %d: Current ride doesn't have destination set, skipping", activeDriver.getId()));
      return false;
    }
    //    update driver's location object before accessing it
    if (activeDriver.getLatitude() > Double.MIN_VALUE) {
      etcCalculationInfo.setLocationObject(activeDriver.getLocationObject());
    }

    //if location object was empty (rare but annoying case), skip the driver
    if (etcCalculationInfo.getLocationObject() == null) {
      final OnlineDriverDto fallback = activeDriverLocationService.getById(activeDriver.getId(), LocationType.ACTIVE_DRIVER);
      if (fallback != null && fallback.getLocationObject() != null) {
        etcCalculationInfo.setLocationObject(fallback.getLocationObject());
      } else {
        return false;
      }
    }

    Long timeToDrive = mapService.getTimeToDriveCached(rideId, new LatLng(etcCalculationInfo.getLatitude(), etcCalculationInfo.getLongitude()),
      new LatLng(etcCalculationInfo.getEndLat(), etcCalculationInfo.getEndLng()));

    return timeToDrive != null && timeToDrive < stackedRidesConfig.getEndRideTimeThreshold(cityId);
  }

}
