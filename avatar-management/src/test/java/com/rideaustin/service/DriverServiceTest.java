package com.rideaustin.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.rideaustin.model.City;
import com.rideaustin.model.Document;
import com.rideaustin.model.enums.AvatarEmailNotificationType;
import com.rideaustin.model.enums.CarInspectionStatus;
import com.rideaustin.model.enums.DocumentType;
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
import com.rideaustin.rest.model.MobileDriverDriverDto;
import com.rideaustin.rest.model.MobileDriverDriverDto.Car;
import com.rideaustin.service.email.EmailService;
import com.rideaustin.service.rating.DriverRatingService;
import com.rideaustin.service.thirdparty.PayoneerService;
import com.rideaustin.service.thirdparty.S3StorageService;
import com.rideaustin.service.user.CarTypesCache;
import com.rideaustin.service.user.DriverTypeCache;
import com.rideaustin.service.user.DriverTypeUtils;

public class DriverServiceTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private DriverDslRepository driverDslRepository;
  @Mock
  private DocumentDslRepository documentDslRepository;
  @Mock
  private UserDslRepository userDslRepository;
  @Mock
  private PayoneerService payoneerService;
  @Mock
  private DriverRatingService driverRatingService;
  @Mock
  private CityService cityService;
  @Mock
  private DocumentService documentService;
  @Mock
  private EmailService emailService;
  @Mock
  private MapService mapService;
  @Mock
  private CurrentUserService currentUserService;
  @Mock
  private S3StorageService s3StorageService;
  @Mock
  private BaseAvatarService baseAvatarService;
  @Mock
  private TermsService termsService;
  @Mock
  private DriverTypeCache driverTypeCache;
  @Mock
  private CarTypesCache carTypesCache;
  
  private DriverService testedInstance;

  private Driver driver = new Driver();
  private User user = new User();

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    testedInstance = new DriverService(driverDslRepository, documentDslRepository, userDslRepository, payoneerService,
      driverRatingService, cityService, documentService, emailService, mapService, currentUserService, s3StorageService,
      baseAvatarService, termsService, driverTypeCache, carTypesCache);

    driver.setUser(user);
    when(currentUserService.getUser()).thenReturn(driver.getUser());

    DriverTypeUtils.setDriverTypeCache(driverTypeCache);
  }

  @Test
  public void testFindDriverById() throws RideAustinException {
    Driver r = new Driver();
    when(driverDslRepository.findById(anyLong())).thenReturn(r);
    Driver driver = testedInstance.findDriver(1L);
    assertThat(driver, is(r));
  }

  @Test
  public void testFindDriverByIdNotFound() throws RideAustinException {
    when(driverDslRepository.findById(anyLong())).thenReturn(null);
    expectedException.expect(NotFoundException.class);
    testedInstance.findDriver(1L);
  }

  @Test
  public void testFindDriverEnrichLastLoginDate() throws RideAustinException {
    long driverId = 1L;
    when(driverDslRepository.findById(eq(driverId))).thenReturn(driver);

    testedInstance.findDriver(driverId);

    verify(baseAvatarService, times(1)).enrichAvatarWithLastLoginDate(driver);
  }

  @Test
  public void getCurrentDriverThrowsErrorWhenCurrentUserIsNotDriver() throws RideAustinException {
    expectedException.expect(ForbiddenException.class);
    expectedException.expectMessage("Current user is not a driver");

    testedInstance.getCurrentDriver();
  }

  @Test
  public void getCurrentDriverThrowsErrorWhenCurrentDriverNotFound() throws RideAustinException {
    user.addAvatar(driver);

    expectedException.expect(NotFoundException.class);
    expectedException.expectMessage("Driver not found");

    testedInstance.getCurrentDriver();
  }

  @Test
  public void getCurrentDriverReturnsDriver() throws RideAustinException {
    user.addAvatar(driver);
    when(driverDslRepository.findByUser(user)).thenReturn(driver);

    final Driver result = testedInstance.getCurrentDriver();

    assertEquals(driver, result);
  }

  @Test
  public void getCurrentDriverInfoSetsDriverPhoto() throws RideAustinException {
    user.addAvatar(driver);
    final long driverId = 1L;
    final MobileDriverDriverDto driverDto = new MobileDriverDriverDto(driverId, 5.0,
      null, 1L, "abs@dfs.ee", "A", "B", "C",
      "+15125555555", 1L, "1", true, null);
    driverDto.setCars(Collections.emptyList());
    when(driverDslRepository.getCurrentDriver(user)).thenReturn(driverDto);
    final Document photo = new Document();
    photo.setDocumentUrl("url");
    when(documentService.findAvatarDocument(driverId, DocumentType.DRIVER_PHOTO)).thenReturn(photo);

    final MobileDriverDriverDto result = testedInstance.getCurrentDriverInfo();

    assertEquals(photo.getDocumentUrl(), result.getPhotoUrl());
  }

  @Test
  public void getCurrentDriverInfoSetsCarPhoto() throws RideAustinException {
    user.addAvatar(driver);
    final long carId = 1L;
    final long driverId = 1L;
    final MobileDriverDriverDto driverDto = new MobileDriverDriverDto(driverId, 5.0,
      null, 1L, "abs@dfs.ee", "A", "B", "C",
      "+15125555555", 1L, "1", true, null);
    final Car car = new Car(carId, "A", "B", "C", "D", "2019",
      1, true, CarInspectionStatus.APPROVED, false, null);
    driverDto.setCars(Collections.singletonList(car));
    when(driverDslRepository.getCurrentDriver(user)).thenReturn(driverDto);
    final Document photo = new Document();
    photo.setDocumentUrl("url");
    when(documentService.findCarDocument(car.getId(), DocumentType.CAR_PHOTO_FRONT)).thenReturn(photo);

    final MobileDriverDriverDto result = testedInstance.getCurrentDriverInfo();

    assertEquals(photo.getDocumentUrl(), result.getCars().get(0).getPhotoUrl());
  }

  @Test
  public void getCurrentDriverInfoSetsInsurancePhoto() throws RideAustinException {
    user.addAvatar(driver);
    final long carId = 1L;
    final long driverId = 1L;
    final MobileDriverDriverDto driverDto = new MobileDriverDriverDto(driverId, 5.0,
      null, 1L, "abs@dfs.ee", "A", "B", "C",
      "+15125555555", 1L, "1", true, null);
    final Car car = new Car(carId, "A", "B", "C", "D", "2019",
      1, true, CarInspectionStatus.APPROVED, false, null);
    driverDto.setCars(Collections.singletonList(car));
    when(driverDslRepository.getCurrentDriver(user)).thenReturn(driverDto);
    final Document photo = new Document();
    photo.setDocumentUrl("url");
    photo.setValidityDate(new Date());
    when(documentService.findCarDocument(car.getId(), DocumentType.INSURANCE)).thenReturn(photo);

    final MobileDriverDriverDto result = testedInstance.getCurrentDriverInfo();

    assertEquals(photo.getDocumentUrl(), result.getCars().get(0).getInsurancePictureUrl());
    assertEquals(photo.getValidityDate(), result.getCars().get(0).getInsuranceExpiryDate());
  }

  @Test
  public void getDriverInfoThrowsErrorWhenDriverNotFound() throws NotFoundException {
    when(driverDslRepository.getDriverInfo(anyLong())).thenReturn(null);

    expectedException.expect(NotFoundException.class);
    expectedException.expectMessage("Driver not found");

    testedInstance.getDriverInfo(1L);
  }

  @Test
  public void getDriverInfoReturnsObject() throws NotFoundException {
    final MobileDriverDriverDto expected = mock(MobileDriverDriverDto.class);
    when(driverDslRepository.getDriverInfo(anyLong())).thenReturn(expected);

    final MobileDriverDriverDto result = testedInstance.getDriverInfo(1L);

    assertEquals(expected, result);
  }

  @Test
  public void updateDriverPhotoRemovesExistingPhoto() throws RideAustinException {
    final Document existing = new Document();
    when(documentService.findAvatarDocument(driver, DocumentType.DRIVER_PHOTO)).thenReturn(existing);

    testedInstance.updateDriverPhoto(driver, mock(MultipartFile.class));

    assertTrue(existing.isRemoved());
  }

  @Test
  public void createDriverThrowsErrorWhenUserIsAlreadyDriver() throws RideAustinException {
    when(driverDslRepository.findByUser(user)).thenReturn(driver);

    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("Driver data already exists");

    testedInstance.createDriver(driver, mock(MultipartFile.class), mock(MultipartFile.class), 1L);
  }

  @Test
  public void createDriverCreatesDriver() throws RideAustinException {
    driver.setSsn("123456789");
    when(driverDslRepository.saveAs(driver, user)).thenAnswer((Answer<Driver>) invocation -> ((Driver) invocation.getArguments()[0]));
    when(driverDslRepository.getLastDCID()).thenReturn("1000");
    final City city = new City();
    city.setName("AUSTIN");
    city.setContactEmail("asd@sdf.rt");
    when(cityService.getById(anyLong())).thenReturn(city);
    when(payoneerService.getSignupURL(anyString())).thenReturn("url");

    testedInstance.createDriver(driver, mock(MultipartFile.class), mock(MultipartFile.class), 1L);
  }

  @Test
  public void getExpiredLicenseDriversFiltersByLastNotificationDate() {
    final Driver notifiedDriver = new Driver();
    final AvatarEmailNotification notification = new AvatarEmailNotification();
    notification.setType(AvatarEmailNotificationType.DRIVER_LICENSE_EXPIRE);
    notification.setDate(new Date());
    notifiedDriver.setNotifications(ImmutableSet.of(
      notification
    ));
    final Driver unnotifiedDriver = new Driver();
    when(documentDslRepository.findDriversWithExpiredLicenses(any(Date.class))).thenReturn(ImmutableList.of(
      notifiedDriver, unnotifiedDriver
    ));

    final List<Driver> result = testedInstance.getExpiredLicenseDrivers(new Date(), Date.from(Instant.now().minus(10, ChronoUnit.DAYS)));

    assertEquals(1, result.size());
    assertEquals(unnotifiedDriver, result.get(0));
  }

  @Test
  public void saveExpiredLicenseNotificationSavesDriverInfo() {
    testedInstance.saveExpiredLicenseNotification(driver);

    assertEquals(1, driver.getNotifications().size());
    assertEquals(AvatarEmailNotificationType.DRIVER_LICENSE_EXPIRE, driver.getNotifications().iterator().next().getType());
    verify(driverDslRepository).saveAs(driver, currentUserService.getUser());
  }

}
