package com.rideaustin.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.rideaustin.assemblers.CarDtoAssembler;
import com.rideaustin.model.Document;
import com.rideaustin.model.enums.CarInspectionStatus;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Car;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.CarDocumentDslRepository;
import com.rideaustin.repo.dsl.CarDslRepository;
import com.rideaustin.repo.dsl.DocumentDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.CarDto;
import com.rideaustin.service.event.EventsNotificationService;
import com.rideaustin.service.user.CarTypesCache;
import com.rideaustin.service.user.CarTypesUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DriverCarsServiceTest {

  private static final long DRIVER_ID = 1L;
  private static final long CAR_ID1 = 1L;
  private static final long CAR_ID2 = 2L;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  @Mock
  private CurrentUserService currentUserService;
  @Mock
  private CarDslRepository carDslRepository;
  @Mock
  private DriverService driverService;
  @Mock
  private EventsNotificationService notificationService;
  @Mock
  private CarTypesCache carTypesCache;
  @Mock
  private ActiveDriversService activeDriversService;
  @Mock
  private DocumentService documentService;
  @Mock
  private CarDocumentDslRepository carDocumentDslRepository;
  @Mock
  private DocumentDslRepository documentDslRepository;
  @Mock
  private ApplicationEventPublisher publisher;
  @Mock
  private CarDtoAssembler carDtoAssembler;
  @Mock
  private MultipartFile photo;
  @Mock
  private MultipartFile insurance;

  private Driver driver = new Driver();
  private User user = new User();

  private DriverCarsService testedInstance;

  private Set<String> regularPremiumLuxury = Sets.newHashSet("REGULAR", "PREMIUM", "LUXURY");
  private Set<String> regularSuv = Sets.newHashSet("REGULAR", "SUV");
  private Set<String> regularPremium = Sets.newHashSet("REGULAR", "PREMIUM");
  private Set<String> regular = Sets.newHashSet("REGULAR");

  @Before
  public void setup() throws Exception {
    testedInstance = new DriverCarsService(
      carDslRepository,
      carDtoAssembler,
      carDocumentDslRepository,
      documentDslRepository,
      driverService,
      notificationService,
      carTypesCache,
      activeDriversService,
      currentUserService,
      documentService,
      publisher);

    driver.setId(DRIVER_ID);
    driver.setUser(user);

    when(currentUserService.getUser()).thenReturn(user);
    when(driverService.findDriver(DRIVER_ID, user)).thenReturn(driver);

    when(carTypesCache.toBitMask(regularPremiumLuxury)).thenReturn(1 + 4 + 16);
    when(carTypesCache.toBitMask(regularSuv)).thenReturn(1 + 2);
    when(carTypesCache.toBitMask(regularPremium)).thenReturn(1 + 4);
    when(carTypesCache.toBitMask(regular)).thenReturn(1);

    CarTypesUtils.setCarTypesCache(carTypesCache);
  }

  @Test
  public void shouldSelectCar() throws RideAustinException {
    // preconditions
    Car c1 = new Car();
    c1.setId(CAR_ID1);
    c1.setSelected(false);
    c1.setInspectionStatus(CarInspectionStatus.APPROVED);
    c1.setDriver(driver);

    Car c2 = new Car();
    c2.setId(CAR_ID2);
    c2.setInspectionStatus(CarInspectionStatus.APPROVED);
    c2.setSelected(true);
    c2.setDriver(driver);

    driver.getCars().add(c1);
    driver.getCars().add(c2);

    when(carDslRepository.findOne(CAR_ID1)).thenReturn(c1);
    when(carDslRepository.findOne(CAR_ID2)).thenReturn(c2);
    when(carDslRepository.getSelected(driver)).thenReturn(c2);
    when(carDslRepository.save(c1)).thenReturn(c1);
    when(carDslRepository.save(c2)).thenReturn(c2);

    // test
    Car returned = testedInstance.selectDriverCar(CAR_ID1, DRIVER_ID);

    // verify
    assertEquals(c1.getId(), returned.getId());
    assertEquals(returned.isSelected(), true);

    ArgumentCaptor<Car> savedCarCaptor = ArgumentCaptor.forClass(Car.class);
    verify(carDslRepository, times(2)).save(savedCarCaptor.capture());

    List<Car> savedCars = savedCarCaptor.getAllValues();

    Car car1saved = savedCars.stream().filter(car -> car.getId() == CAR_ID1).findFirst()
      .orElseThrow(IllegalStateException::new);
    assertTrue(car1saved.isSelected());

    Car car2saved = savedCars.stream().filter(car -> car.getId() == CAR_ID2).findFirst()
      .orElseThrow(IllegalStateException::new);
    assertFalse(car2saved.isSelected());
  }

  @Test(expected = BadRequestException.class)
  public void shouldThrowErrorOnSelectUnapprovedCar() throws Exception {
    Car c = new Car();
    c.setId(CAR_ID1);
    c.setInspectionStatus(CarInspectionStatus.NOT_INSPECTED);
    c.setDriver(driver);

    when(carDslRepository.findOne(CAR_ID1)).thenReturn(c);

    testedInstance.selectDriverCar(CAR_ID1, DRIVER_ID);
  }

  @Test
  public void testRemoveDriverCarSetsSelectedToFalse() throws Exception {
    Car car = new Car();
    car.setId(CAR_ID1);
    car.setInspectionStatus(CarInspectionStatus.APPROVED);
    car.setDriver(driver);
    when(carDslRepository.findOne(CAR_ID1)).thenReturn(car);

    testedInstance.removeDriverCar(CAR_ID1, driver.getId());

    ArgumentCaptor<Car> captor = ArgumentCaptor.forClass(Car.class);

    verify(carDslRepository, times(1)).save(captor.capture());
    assertFalse(captor.getValue().isSelected());
    assertTrue(captor.getValue().isRemoved());
  }

  @Test(expected = BadRequestException.class)
  public void testRemoveDriverCarDeniesRemovingCurrentSelectedCar() throws Exception {
    Car car = new Car();
    car.setId(CAR_ID1);
    car.setInspectionStatus(CarInspectionStatus.APPROVED);
    car.setDriver(driver);
    when(carDslRepository.findOne(CAR_ID1)).thenReturn(car);
    ActiveDriver activeDriver = ActiveDriver.builder()
      .selectedCar(car)
      .build();
    when(activeDriversService.getCurrentActiveDriver()).thenReturn(activeDriver);

    testedInstance.removeDriverCar(CAR_ID1, driver.getId());
  }

  @Test
  public void addCarUploadsInsurance() throws RideAustinException {
    final CarDto carDto = new CarDto();
    final Date insuranceExpiryDate = new Date();
    carDto.setInsuranceExpiryDate(insuranceExpiryDate);
    final Driver driver = new Driver();
    driver.setCityId(1L);
    driver.setId(1L);
    driver.setCars(new HashSet<>(ImmutableSet.of(new Car())));
    when(driverService.findDriver(eq(1L), any(User.class))).thenReturn(driver);
    when(carDtoAssembler.toDs(carDto)).thenReturn(new Car());
    final Document photoDocument = new Document();
    photoDocument.setDocumentType(DocumentType.CAR_PHOTO_FRONT);
    when(carDocumentDslRepository.findCarPhotos(anyLong())).thenReturn(ImmutableList.of(photoDocument));
    when(carDslRepository.save(any(Car.class))).thenAnswer((Answer<Car>) invocationOnMock -> (Car) invocationOnMock.getArguments()[0]);

    testedInstance.addCar(1L, carDto, photo, insurance);

    verify(documentService).uploadDocument(insurance, DocumentType.INSURANCE, carDto.getInsuranceExpiryDate(), null, 1L);
  }

  @Test
  public void addCarCreatesInsuranceFromExisting() throws RideAustinException {
    final CarDto carDto = new CarDto();
    final Date insuranceExpiryDate = new Date();
    carDto.setInsuranceExpiryDate(insuranceExpiryDate);
    final Driver driver = new Driver();
    driver.setCityId(1L);
    driver.setId(1L);
    when(driverService.findDriver(eq(1L), any(User.class))).thenReturn(driver);
    when(carDtoAssembler.toDs(carDto)).thenReturn(new Car());
    final Document photoDocument = new Document();
    photoDocument.setDocumentType(DocumentType.CAR_PHOTO_FRONT);
    when(carDocumentDslRepository.findCarPhotos(anyLong())).thenReturn(ImmutableList.of(photoDocument));
    when(carDslRepository.save(any(Car.class))).thenAnswer((Answer<Car>) invocationOnMock -> (Car) invocationOnMock.getArguments()[0]);

    testedInstance.addCar(1L, carDto, photo, null);

    verify(documentService).createDocument(DocumentType.INSURANCE, driver.getInsurancePictureUrl(), driver.getInsuranceExpiryDate(), null, 1L);
  }

  @Test
  public void editCarThrowsErrorWhenCarNotFound() throws RideAustinException {
    when(driverService.findDriver(eq(1L), any(User.class))).thenReturn(new Driver());
    when(carDslRepository.findOne(1L)).thenReturn(null);

    expectedException.expect(NotFoundException.class);
    expectedException.expectMessage("Car not found");

    testedInstance.editCar(1L, 1L, new CarDto());
  }

  @Test
  public void editCarThrowsErrorWhenCarIsRemoved() throws RideAustinException {
    when(driverService.findDriver(eq(1L), any(User.class))).thenReturn(new Driver());
    final Car car = new Car();
    car.setRemoved(true);
    when(carDslRepository.findOne(1L)).thenReturn(car);

    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("The car has been removed and cannot be edited");

    testedInstance.editCar(1L, 1L, new CarDto());
  }

  @Test
  public void editCarThrowsErrorWhenDriversDontMatch() throws RideAustinException {
    when(driverService.findDriver(eq(1L), any(User.class))).thenReturn(new Driver());
    final Car car = new Car();
    final Driver driver = new Driver();
    driver.setId(2L);
    car.setDriver(driver);
    when(carDslRepository.findOne(1L)).thenReturn(car);

    expectedException.expect(NotFoundException.class);
    expectedException.expectMessage("Car not found");

    testedInstance.editCar(1L, 1L, new CarDto());
  }

  @Test
  public void editCarSendsCarCategoryNotification() throws RideAustinException {
    final Driver driver = new Driver();
    driver.setId(1L);
    when(driverService.findDriver(eq(1L), any(User.class))).thenReturn(driver);
    final Car currentCar = new Car();
    currentCar.setDriver(driver);
    currentCar.setSelected(true);
    currentCar.setCarCategoriesBitmask(1);
    when(carDslRepository.findOne(1L)).thenReturn(currentCar);
    final Car newCar = new Car();
    when(carDtoAssembler.toDs(any(CarDto.class))).thenReturn(newCar);
    when(carTypesCache.toBitMask(anySetOf(String.class))).thenReturn(2);

    testedInstance.editCar(1L, 1L, new CarDto());

    verify(notificationService).sendCarCategoryChange(driver.getId());
    verify(activeDriversService).adjustActiveDriverAvailableCarCategories(any(Car.class), eq(driver));
  }

}
