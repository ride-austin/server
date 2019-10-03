package com.rideaustin.service;

import static com.rideaustin.utils.SafeZeroUtils.safeZero;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableList;
import com.rideaustin.events.CityApprovalStatusUpdateEvent;
import com.rideaustin.events.OnboardingUpdateEvent;
import com.rideaustin.model.Document;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.enums.ApiClientAppType;
import com.rideaustin.model.enums.CarInspectionStatus;
import com.rideaustin.model.enums.CityApprovalStatus;
import com.rideaustin.model.enums.DocumentStatus;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.enums.DriverActivationStatus;
import com.rideaustin.model.enums.DriverOnboardingStatus;
import com.rideaustin.model.enums.DriverSpecialFlags;
import com.rideaustin.model.enums.PayoneerStatus;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Car;
import com.rideaustin.model.ride.DriverType;
import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.dsl.AvatarDocumentDslRepository;
import com.rideaustin.repo.dsl.DocumentDslRepository;
import com.rideaustin.repo.dsl.DriverDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.rest.model.ConsoleDriverDto;
import com.rideaustin.rest.model.DriverOnboardingInfo;
import com.rideaustin.rest.model.DriverStatusPendingDto;
import com.rideaustin.rest.model.InspectionStickerStatus;
import com.rideaustin.rest.model.ListDriversParams;
import com.rideaustin.rest.model.ListDriversWithDocumentsParams;
import com.rideaustin.rest.model.PagingParams;
import com.rideaustin.service.event.EventsNotificationService;
import com.rideaustin.service.model.AvatarUpdateDto;
import com.rideaustin.service.ride.DirectConnectService;
import com.rideaustin.service.thirdparty.PayoneerService;
import com.rideaustin.service.user.DriverTypeCache;
import com.rideaustin.service.user.DriverTypeUtils;
import com.rideaustin.utils.DriverUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DriverAdministrationService {

  private final DriverDslRepository driverDslRepository;
  private final AvatarDocumentDslRepository avatarDocumentDslRepository;
  private final DocumentDslRepository documentDslRepository;

  private final UserService userService;
  private final EventsNotificationService notificationService;
  private final DirectConnectService directConnectService;
  private final DriverEmailReminderService driverEmailReminderService;
  private final SessionService sessionService;
  private final CurrentSessionService currentSessionService;
  private final DriverTypeCache driverTypeCache;
  private final BaseAvatarService baseAvatarService;
  private final PayoneerService payoneerService;
  private final CurrentUserService currentUserService;
  private final DriverService driverService;
  private final ActiveDriversService activeDriversService;
  private final RideDslRepository rideDslRepository;

  private final ApplicationEventPublisher publisher;

  public Page<Driver> listDrivers(ListDriversParams params, PagingParams paging) {
    Page<Driver> drivers = driverDslRepository.findDrivers(params, paging);
    baseAvatarService.enrichAvatarWithLastLoginDate(drivers);
    return drivers;
  }

  public List<Driver> listDrivers(ListDriversParams params) {
    List<Driver> drivers = driverDslRepository.findDrivers(params);
    baseAvatarService.enrichAvatarWithLastLoginDate(drivers);
    return drivers;
  }

  /**
   * Check and update status for all drivers with payoneer status = "PENDING"
   *
   * @param updateAll If updateALL param == false - check only created last week.
   */
  public boolean checkAndUpdatePayoneerStatusForPendingDrivers(boolean updateAll) {
    boolean success = true;
    ListDriversParams params = new ListDriversParams();
    params.setPayoneerStatus(Collections.singletonList(PayoneerStatus.PENDING));
    if (!updateAll) {
      params.setCreatedOnAfter(Instant.now().minus(7, ChronoUnit.DAYS));
    }
    for (Driver driver : driverDslRepository.findDrivers(params)) {
      try {
        DriverOnboardingInfo copy = DriverUtils.createCopy(driver);
        driver.setPayoneerStatus(payoneerService.getPayoneerStatus(driver.getPayoneerId()));
        publisher.publishEvent(new OnboardingUpdateEvent<>(copy, DriverUtils.createCopy(driver), driver.getId()));
        driverDslRepository.saveAs(driver, currentUserService.getUser());
      } catch (RideAustinException e) {
        log.error(String.format("Failed to update driver %d payoneer status", driver.getId()), e);
        success = false;
      }
    }
    return success;
  }

  public Map<DriverOnboardingStatus, Long> listDriverStatuses(Long cityId) {
    return driverDslRepository.findDriverCountsByStatus(cityId);
  }

  public DriverStatusPendingDto listDriverPendingStatuses(Long cityId) {
    DriverStatusPendingDto.Builder builder = new DriverStatusPendingDto.Builder();

    ListDriversParams carInspectionParams = createParams(cityId, ListDriversParams::setCarInspectionStatus,
      ImmutableList.of(CarInspectionStatus.NOT_INSPECTED, CarInspectionStatus.PENDING));

    ListDriversParams cityApprovalParams = createParams(cityId, ListDriversParams::setCityApprovalStatus,
      ImmutableList.of(CityApprovalStatus.PENDING, CityApprovalStatus.NOT_PROVIDED, CityApprovalStatus.REJECTED_BY_CITY,
        CityApprovalStatus.REJECTED_PHOTO, CityApprovalStatus.EXPIRED));

    ListDriversWithDocumentsParams inspectionStickerParams = createDocumentParams(cityId, ListDriversWithDocumentsParams::setInspectionStickerStatus,
      ImmutableList.of(InspectionStickerStatus.PENDING, InspectionStickerStatus.EXPIRED, InspectionStickerStatus.REJECTED));

    ListDriversWithDocumentsParams insuranceParams = createDocumentParams(cityId, ListDriversWithDocumentsParams::setInsuranceStatus);
    ListDriversWithDocumentsParams licenseParams = createDocumentParams(cityId, ListDriversWithDocumentsParams::setDriverLicenseStatus);

    ListDriversParams payoneerParams = createParams(cityId, ListDriversParams::setPayoneerStatus,
      ImmutableList.of(PayoneerStatus.INITIAL, PayoneerStatus.PENDING));

    long pendingProfilePhotosCount = avatarDocumentDslRepository.getDriversWithoutProfilePhotosCount(cityId);
    long pendingInspectionStickerCount = driverDslRepository.getDriversCount(inspectionStickerParams);
    long pendingCarPhotosCount = documentDslRepository.getDriversCountWithoutCarPhotos(cityId);
    long pendingCarInspectionCount = driverDslRepository.getDriversCount(carInspectionParams);
    return builder
      .carInspection(pendingCarInspectionCount)
      .carPhotos(pendingCarPhotosCount)
      .cityApproval(driverDslRepository.getDriversCount(cityApprovalParams))
      .driverLicense(driverDslRepository.getDriversCount(licenseParams))
      .inspectionSticker(pendingInspectionStickerCount)
      .insurance(driverDslRepository.getDriversCount(insuranceParams))
      .profilePhotos(pendingProfilePhotosCount)
      .payoneer(driverDslRepository.getDriversCount(payoneerParams))
      .build();
  }

  public void saveDriverTypes(long driverId, List<String> driverTypes) throws RideAustinException {
    Driver driver = driverService.findDriver(driverId, currentUserService.getUser());
    final Integer oldGranted = driver.getGrantedDriverTypesBitmask();
    final Integer newGranted = safeZero(driverTypeCache.toBitMask(driverTypes));
    driver.setGrantedDriverTypesBitmask(newGranted);
    if (!Objects.equals(oldGranted, newGranted)) {
      notificationService.sendDriverTypeChange(driver);
    }
    driverDslRepository.saveAs(driver, currentUserService.getUser());
  }

  public void sendActivationEmail(long id) throws RideAustinException {
    Driver driver = driverService.findDriver(id, currentUserService.getUser());
    if (!driver.isActive()) {
      throw new BadRequestException("Driver is not active");
    }
    if (driver.getPayoneerId() == null || driver.getPayoneerStatus() == PayoneerStatus.INITIAL) {
      driver.initPayoneerFields();
    }
    driverDslRepository.saveAs(driver, currentUserService.getUser());
    driverEmailReminderService.sendActivationEmail(id, driver.getCityId());
  }

  public void disableDriverImmediately(Long driverId) {
    Driver driver = driverDslRepository.findById(driverId);
    driver.setActivationStatus(DriverActivationStatus.SUSPENDED);
    driver.setActive(false);
    driverDslRepository.saveAs(driver, currentUserService.getUser());
    sessionService.endSessionsImmediately(driver.getUser());
  }

  public Pair<ConsoleDriverDto, Set<Car>> getDriver(long id) throws RideAustinException {
    Driver driver = driverDslRepository.findById(id);
    if (driver == null) {
      throw new NotFoundException("Driver not found");
    }
    updatePayoneerStatus(driver);

    ConsoleDriverDto result = driverDslRepository.findAdminInfo(id);
    Document chauffeurLicense = documentDslRepository.findByAvatarAndType(id, DocumentType.CHAUFFEUR_LICENSE);
    boolean chauffeurLicenseValid = chauffeurLicense != null && EnumSet.of(DocumentStatus.APPROVED, DocumentStatus.PENDING).contains(chauffeurLicense.getDocumentStatus());
    result.setChauffeurLicense(chauffeurLicenseValid && safeZero(driver.getGrantedDriverTypesBitmask()) >= driverTypeCache.toBitMask(Collections.singleton(DriverType.DIRECT_CONNECT)));
    final ActiveDriver activeDriver = activeDriversService.getActiveDriverByDriver(driver.getUser());
    if (activeDriver != null) {
      if (activeDriver.getStatus() == ActiveDriverStatus.AVAILABLE) {
        result.setOnlineStatus(ConsoleDriverDto.OnlineDriverStatus.ONLINE);
      } else if (CollectionUtils.isEmpty(rideDslRepository.findByActiveDriverAndStatuses(activeDriver, RideStatus.ONGOING_DRIVER_STATUSES))) {
        result.setOnlineStatus(ConsoleDriverDto.OnlineDriverStatus.STUCK);
      } else {
        result.setOnlineStatus(ConsoleDriverDto.OnlineDriverStatus.RIDING);
      }
    }
    return ImmutablePair.of(result, driver.getCars());
  }

  public ConsoleDriverDto updateDriver(long id, @Nonnull ConsoleDriverDto driver) throws RideAustinException {
    Driver current = driverService.findDriver(id, currentUserService.getUser());
    userService.checkPhoneNumberAvailable(current.getPhoneNumber(), driver.getUser().getPhoneNumber());
    OnboardingUpdateEvent<DriverOnboardingInfo> event = new OnboardingUpdateEvent<>(DriverUtils.createCopy(current), driver, current.getId());
    boolean publishEvents = current.getCityApprovalStatus() != driver.getCityApprovalStatus();
    if (!Objects.equals(current.getDirectConnectId(), driver.getDirectConnectId())) {
      directConnectService.validateDirectConnectId(driver.getDirectConnectId());
      current.setDirectConnectId(driver.getDirectConnectId());
    }

    current.getUser().setDateOfBirth(driver.getUser().getDateOfBirth());
    current.getUser().setAddress(driver.getUser().getAddress());
    current.getUser().setEmail(driver.getUser().getEmail());
    current.getUser().setGender(driver.getUser().getGender());
    current.getUser().setFirstname(driver.getUser().getFirstname());
    current.getUser().setMiddleName(driver.getUser().getMiddleName());
    current.getUser().setLastname(driver.getUser().getLastname());
    current.getUser().setNickName(driver.getUser().getNickName());
    current.getUser().setPhoneNumber(driver.getUser().getPhoneNumber());

    current.setLicenseNumber(driver.getLicenseNumber());
    current.setLicenseState(driver.getLicenseState());
    current.setActivationNotes(driver.getActivationNotes());
    if (current.getActivationStatus() != driver.getActivationStatus()) {
      baseAvatarService.updateAvatarByAdmin(new AvatarUpdateDto(current), new AvatarUpdateDto(current.getFullName(), current.getEmail(),
        current.getCityId(), DriverActivationStatus.ACTIVE.equals(driver.getActivationStatus())));
    }
    if (DriverActivationStatus.ACTIVE.equals(driver.getActivationStatus())) {
      current.setActivationDate(new Date());
      current.setActive(true);
    } else {
      current.setActive(false);
    }
    current.setActivationStatus(driver.getActivationStatus());
    current.setCityApprovalStatus(driver.getCityApprovalStatus());
    current.setSpecialFlags(DriverSpecialFlags.toBitMask(driver.getSpecialFlags()));

    Integer oldBitmask = current.getGrantedDriverTypesBitmask();
    Integer directConnectBitmask = DriverTypeUtils.toBitMask(Collections.singleton(DriverType.DIRECT_CONNECT));
    Integer fingerprintedBitmask = DriverTypeUtils.toBitMask(Collections.singleton(DriverType.FINGERPRINTED));
    Integer newBitmask = safeZero(DriverTypeUtils.toBitMask(driver.getGrantedDriverTypes()));
    newBitmask = updateBitmask(directConnectBitmask, newBitmask, driver.isChauffeurLicense());
    newBitmask = updateBitmask(fingerprintedBitmask, newBitmask, CityApprovalStatus.APPROVED.equals(driver.getCityApprovalStatus()));
    current.setGrantedDriverTypesBitmask(newBitmask);

    if (!Objects.equals(newBitmask, oldBitmask)) {
      notificationService.sendDriverTypeChange(current);
    }
    if (driver.getSsn() != null) {
      current.setSsn(DriverUtils.maskSsn(driver.getSsn()));
    }
    if (publishEvents) {
      publisher.publishEvent(new CityApprovalStatusUpdateEvent(driver.getCityApprovalStatus(), current));
    }
    publisher.publishEvent(event);
    currentSessionService.refreshUserSession(current.getUser(), ApiClientAppType.MOBILE_DRIVER);
    driverDslRepository.saveAs(current, currentUserService.getUser());
    return driver;
  }

  protected Integer updateBitmask(Integer modifierBitmask, Integer existingBitmask, boolean condition) {
    Integer newBitmask = existingBitmask;
    if (condition) {
      newBitmask |= modifierBitmask;
    } else if ((newBitmask & modifierBitmask) > 0) {
      newBitmask ^= modifierBitmask;
    }
    return newBitmask;
  }

  private void updatePayoneerStatus(Driver driver) throws RideAustinException {
    try {
      String payoneerId = driver.getPayoneerId();
      if (StringUtils.isNotEmpty(payoneerId)
        && !PayoneerStatus.ACTIVE.equals(driver.getPayoneerStatus())
        && PayoneerStatus.ACTIVE.equals(payoneerService.getPayoneerStatus(payoneerId))) {
        DriverOnboardingInfo copy = DriverUtils.createCopy(driver);
        driver.setPayoneerStatus(PayoneerStatus.ACTIVE);
        publisher.publishEvent(new OnboardingUpdateEvent<>(copy, driver, driver.getId()));
        driverDslRepository.saveAs(driver, currentUserService.getUser());
      }
    } catch (ServerError e) {
      log.error("Failed to update Payoneer status", e);
    }
  }

  private ListDriversWithDocumentsParams createDocumentParams(Long cityId, BiConsumer<ListDriversWithDocumentsParams, List> setter) {
    return createDocumentParams(cityId, setter, ImmutableList.of(DocumentStatus.PENDING, DocumentStatus.EXPIRED, DocumentStatus.REJECTED));
  }

  private ListDriversWithDocumentsParams createDocumentParams(Long cityId, BiConsumer<ListDriversWithDocumentsParams, List> setter, List<?> values) {
    ListDriversWithDocumentsParams params = new ListDriversWithDocumentsParams();
    params.setCityId(cityId);
    setter.accept(params, values);
    return params;
  }

  private ListDriversParams createParams(Long cityId, BiConsumer<ListDriversParams, List> setter, List<?> values) {
    ListDriversParams params = new ListDriversParams();
    params.setCityId(cityId);
    setter.accept(params, values);
    return params;
  }
}
