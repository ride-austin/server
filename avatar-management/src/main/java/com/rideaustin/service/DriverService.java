package com.rideaustin.service;

import static com.rideaustin.utils.SafeZeroUtils.safeZero;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.apache.commons.mail.EmailException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.rideaustin.model.Document;
import com.rideaustin.model.enums.AvatarEmailNotificationType;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.ride.DriverType;
import com.rideaustin.model.user.AvatarEmailNotification;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.DocumentDslRepository;
import com.rideaustin.repo.dsl.DriverDslRepository;
import com.rideaustin.repo.dsl.UserDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.ForbiddenException;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.rest.model.MobileDriverDriverDto;
import com.rideaustin.service.email.EmailService;
import com.rideaustin.service.rating.DriverRatingService;
import com.rideaustin.service.thirdparty.PayoneerService;
import com.rideaustin.service.thirdparty.S3StorageService;
import com.rideaustin.service.user.CarTypesCache;
import com.rideaustin.service.user.DriverSignupEmail;
import com.rideaustin.service.user.DriverTypeCache;
import com.rideaustin.service.user.DriverTypeUtils;
import com.rideaustin.utils.DirectConnectUtils;
import com.rideaustin.utils.DriverUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DriverService {

  private static final String DRIVER_NOT_FOUND_MESSAGE = "Driver not found";
  private static final Long DEFAULT_TERM_ID = 1L;

  private final DriverDslRepository driverDslRepository;
  private final DocumentDslRepository documentDslRepository;
  private final UserDslRepository userDslRepository;

  private final PayoneerService payoneerService;
  private final DriverRatingService driverRatingService;
  private final CityService cityService;
  private final DocumentService documentService;
  private final EmailService emailService;
  private final MapService mapService;
  private final CurrentUserService currentUserService;
  private final S3StorageService s3StorageService;
  private final BaseAvatarService baseAvatarService;
  private final TermsService termsService;

  private final DriverTypeCache driverTypeCache;
  private final CarTypesCache carTypesCache;

  public Driver getCurrentDriver() throws RideAustinException {
    return this.getDriver(driverDslRepository::findByUser);
  }

  public MobileDriverDriverDto getCurrentDriverInfo() throws RideAustinException {
    MobileDriverDriverDto currentDriver = getDriver(driverDslRepository::getCurrentDriver);
    return fillDriverInfo(currentDriver);
  }

  public MobileDriverDriverDto getDriverInfo(long id) throws NotFoundException {
    MobileDriverDriverDto currentDriver = driverDslRepository.getDriverInfo(id);
    if (currentDriver == null) {
      throw new NotFoundException(DRIVER_NOT_FOUND_MESSAGE);
    }
    return fillDriverInfo(currentDriver);
  }

  public void updateDriverPhoto(@Nonnull Driver driver, @Nonnull MultipartFile photoData) throws RideAustinException {
    Document existingPhoto = documentService.findAvatarDocument(driver, DocumentType.DRIVER_PHOTO);
    if (existingPhoto != null) {
      existingPhoto.setRemoved(true);
      documentDslRepository.save(existingPhoto);
    }
    Document photo = documentService.uploadPublicDocument(photoData, DocumentType.DRIVER_PHOTO, driver.getCityId(), driver.getId());
    documentService.saveAvatarDocument(driver, photo);
    driverDslRepository.saveAs(driver, currentUserService.getUser());
  }

  public Driver createDriver(Driver d, MultipartFile licenseData, MultipartFile insuranceData, Long termId) throws RideAustinException {
    User user = currentUserService.getUser();
    if (driverDslRepository.findByUser(user) != null) {
      throw new BadRequestException("Driver data already exists");
    }
    Driver driver = new Driver();
    driver.setAgreementDate(new Date());
    driver.setCityId(d.getCityId());

    driver.setSsn(DriverUtils.maskSsn(d.getSsn()));
    driver.setLicenseNumber(d.getLicenseNumber());
    driver.setLicenseState(d.getLicenseState());
    driver.setUser(user);
    driver.setRating(driverRatingService.getDefaultRating());

    User dUser = d.getUser();
    user.setAddress(mapService.normalizeAddress(dUser.getAddress()));
    user.setDateOfBirth(dUser.getDateOfBirth());
    user.addAvatar(driver);
    user.setFirstname(DriverUtils.fixNameCase(dUser.getFirstname()));
    user.setMiddleName(DriverUtils.fixNameCase(dUser.getMiddleName()));
    user.setLastname(DriverUtils.fixNameCase(dUser.getLastname()));

    user = userDslRepository.save(user);
    driver = driverDslRepository.saveAs(driver, currentUserService.getUser());

    Document license = documentService.uploadDocument(licenseData, DocumentType.LICENSE, d.getLicenseExpiryDate(), null, driver.getId());
    documentService.saveAvatarDocument(driver, license);
    termsService.acceptTerms(driver, Optional.ofNullable(termId).orElse(DEFAULT_TERM_ID));

    try {
      driver.setInsurancePictureUrl(s3StorageService.uploadPrivateFile(DocumentType.INSURANCE.getFolderName(), insuranceData.getBytes()));
      driver.setInsuranceExpiryDate(d.getInsuranceExpiryDate());
    } catch (IOException e) {
      log.error("Error while uploading insurance", e);
    }

    driver.initPayoneerFields();
    driver.setDirectConnectId(DirectConnectUtils.generateNextId(driverDslRepository.getLastDCID()));

    driver = driverDslRepository.saveAs(driver, currentUserService.getUser());

    sendDriverSignupEmail(driver);

    currentUserService.setUser(user);

    return driver;
  }

  public List<Driver> getExpiredLicenseDrivers(Date expirationLimit, Date notificationLimit) {
    List<Driver> drivers = documentDslRepository.findDriversWithExpiredLicenses(expirationLimit);

    return drivers.stream()
      .filter(driver -> driver.getNotifications().stream()
        .filter(n -> AvatarEmailNotificationType.DRIVER_LICENSE_EXPIRE.equals(n.getType()))
        .allMatch(n -> n.getDate().before(notificationLimit)))
      .collect(Collectors.toList());
  }

  public void saveExpiredLicenseNotification(Driver driver) {
    AvatarEmailNotification notification = new AvatarEmailNotification();
    notification.setType(AvatarEmailNotificationType.DRIVER_LICENSE_EXPIRE);
    notification.setDate(new Date());
    driver.getNotifications().add(notification);
    driverDslRepository.saveAs(driver, currentUserService.getUser());
  }

  @Nonnull
  public Driver findDriver(long id) throws NotFoundException {
    Driver driver = driverDslRepository.findById(id);
    if (driver == null) {
      throw new NotFoundException(DRIVER_NOT_FOUND_MESSAGE);
    }
    baseAvatarService.enrichAvatarWithLastLoginDate(driver);
    return driver;
  }

  @Nonnull
  public Driver findDriver(long id, User user) throws RideAustinException {
    Driver driver = findDriver(id);
    driver.checkAccess(user);
    log.debug("Loaded DRIVER's avatars: {}", driver.getUser().getAvatars().size());
    return driver;
  }

  void sendDriverSignupEmail(Driver driver) throws RideAustinException {
    try {
      emailService.sendEmail(new DriverSignupEmail(driver, cityService.getById(driver.getCityId()), payoneerService.getSignupURL(driver.getPayoneerId())));
    } catch (EmailException e) {
      throw new ServerError(e);
    }
  }

  private MobileDriverDriverDto fillDriverInfo(MobileDriverDriverDto currentDriver) {
    Document photo = documentService.findAvatarDocument(currentDriver.getId(), DocumentType.DRIVER_PHOTO);
    if (photo != null) {
      currentDriver.setPhotoUrl(photo.getDocumentUrl());
    }
    currentDriver.setGrantedDriverTypes(driverTypeCache.fromBitMask(currentDriver.getGrantedDriverTypesBitmask()));
    for (MobileDriverDriverDto.Car car : currentDriver.getCars()) {
      car.setCarCategories(carTypesCache.fromBitMask(car.getCarCategoriesBitmask()));
      Document carPhoto = documentService.findCarDocument(car.getId(), DocumentType.CAR_PHOTO_FRONT);
      if (carPhoto != null) {
        car.setPhotoUrl(carPhoto.getDocumentUrl());
      }
      Document insurance = documentService.findCarDocument(car.getId(), DocumentType.INSURANCE);
      if (insurance != null) {
        car.setInsuranceExpiryDate(insurance.getValidityDate());
        car.setInsurancePictureUrl(insurance.getDocumentUrl());
      }
    }
    Document chauffeurPermit = documentService.findAvatarDocument(currentDriver.getId(), DocumentType.CHAUFFEUR_LICENSE);
    boolean isDCEnabled = (safeZero(currentDriver.getGrantedDriverTypesBitmask()) & DriverTypeUtils.toBitMask(Collections.singleton(DriverType.DIRECT_CONNECT))) > 0;
    currentDriver.setChauffeurPermit(chauffeurPermit != null && isDCEnabled);
    return currentDriver;
  }

  @NotNull
  private <T> T getDriver(final Function<User, T> retriever) throws ForbiddenException, NotFoundException {
    User currentUser = currentUserService.getUser();
    if (!currentUser.hasAvatar(AvatarType.DRIVER)) {
      throw new ForbiddenException("Current user is not a driver");
    }
    T driver = retriever.apply(currentUser);
    if (driver == null) {
      throw new NotFoundException(DRIVER_NOT_FOUND_MESSAGE);
    }
    return driver;
  }

}

