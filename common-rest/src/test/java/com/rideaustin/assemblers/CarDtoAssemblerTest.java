package com.rideaustin.assemblers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.model.Document;
import com.rideaustin.model.enums.CarInspectionStatus;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.ride.Car;
import com.rideaustin.repo.dsl.CarDocumentDslRepository;
import com.rideaustin.rest.model.CarDto;
import com.rideaustin.service.thirdparty.S3StorageService;
import com.rideaustin.service.user.CarTypesCache;
import com.rideaustin.service.user.CarTypesUtils;

public class CarDtoAssemblerTest {

  private static final int REGULAR_BITMASK = 1;
  private static final String HTTPS_URL = "https://url";
  private static final String URL = "url";
  private CarDtoAssembler testedInstance;

  @Mock
  private S3StorageService s3StorageService;
  @Mock
  private CarDocumentDslRepository carDocumentDslRepository;
  @Mock
  private CarTypesCache carTypesCache;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    when(carTypesCache.toBitMask(anySetOf(String.class))).thenReturn(REGULAR_BITMASK);
    CarTypesUtils.setCarTypesCache(carTypesCache);

    testedInstance = new CarDtoAssembler(s3StorageService, carDocumentDslRepository);
  }

  @Test
  public void toDsSkipsNull() {
    final Car result = testedInstance.toDs((CarDto) null);

    assertNull(result);
  }

  @Test
  public void toDsSetsCategoriesBitmask() {
    CarDto carDto = new CarDto();
    carDto.setCarCategories(Collections.singleton("REGULAR"));

    final Car result = testedInstance.toDs(carDto);

    assertEquals(REGULAR_BITMASK, result.getCarCategoriesBitmask());
  }

  @Test
  public void toDsSetsSelected() {
    CarDto carDto = new CarDto();
    carDto.setSelected(true);

    final Car result = testedInstance.toDs(carDto);

    assertTrue(result.isSelected());
  }

  @Test
  public void toDsSetsRemoved() {
    CarDto carDto = new CarDto();
    carDto.setRemoved(true);

    final Car result = testedInstance.toDs(carDto);

    assertTrue(result.isRemoved());
  }

  @Test
  public void toDtoSkipsNull() {
    final CarDto carDto = testedInstance.toDto((Car) null);

    assertNull(carDto);
  }

  @Test
  public void toDtoSetsBaseFields() {
    Car car = createCar();

    final CarDto result = testedInstance.toDto(car);

    assertEquals(car.getId(), result.getId());
    assertEquals(car.getColor(), result.getColor());
    assertEquals(car.getMake(), result.getMake());
    assertEquals(car.getModel(), result.getModel());
    assertEquals(car.getYear(), result.getYear());
    assertEquals(car.isSelected(), result.getSelected());
    assertEquals(car.getInspectionStatus(), result.getInspectionStatus());
    assertEquals(car.getInspectionNotes(), result.getInspectionNotes());
    assertEquals(car.isRemoved(), result.getRemoved());
  }

  @Test
  public void testToDtoSetsInsuranceFields() {
    Car car = createCar();
    final Document insurance = new Document();
    final Date validityDate = new Date();
    insurance.setDocumentUrl(URL);
    insurance.setValidityDate(validityDate);
    when(carDocumentDslRepository.findByCarAndType(eq(car.getId()), eq(DocumentType.INSURANCE))).thenReturn(insurance);
    when(s3StorageService.getSignedURL(anyString())).thenReturn(HTTPS_URL);

    final CarDto result = testedInstance.toDto(car);

    assertEquals(validityDate, result.getInsuranceExpiryDate());
    assertEquals(HTTPS_URL, result.getInsurancePictureUrl());
  }

  @Test
  public void testToDtoSetsPhotoFields() {
    Car car = createCar();
    final Document photo = new Document();
    photo.setDocumentUrl(URL);

    when(carDocumentDslRepository.findByCarAndType(eq(car.getId()), eq(DocumentType.CAR_PHOTO_FRONT))).thenReturn(photo);

    final CarDto result = testedInstance.toDto(car);

    assertEquals(URL, result.getPhotoUrl());
  }

  private Car createCar() {
    final long id = 1L;
    final String color = "Color";
    final String make = "Make";
    final String model = "Model";
    final String year = "2019";
    final boolean selected = true;
    final CarInspectionStatus inspectionStatus = CarInspectionStatus.APPROVED;
    final String notes = "Notes";
    final boolean removed = false;
    Car car = new Car();
    car.setId(id);
    car.setColor(color);
    car.setMake(make);
    car.setModel(model);
    car.setYear(year);
    car.setSelected(selected);
    car.setInspectionStatus(inspectionStatus);
    car.setInspectionNotes(notes);
    car.setRemoved(removed);
    return car;
  }
}