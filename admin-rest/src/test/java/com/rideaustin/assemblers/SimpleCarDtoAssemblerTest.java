package com.rideaustin.assemblers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.model.Document;
import com.rideaustin.model.enums.CarInspectionStatus;
import com.rideaustin.model.enums.DocumentStatus;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.ride.Car;
import com.rideaustin.model.user.Driver;
import com.rideaustin.rest.model.SimpleCarDto;
import com.rideaustin.service.DocumentService;
import com.rideaustin.service.thirdparty.S3StorageService;

public class SimpleCarDtoAssemblerTest {

  @Mock
  private DocumentService documentService;
  @Mock
  private S3StorageService s3StorageService;

  private SimpleCarDtoAssembler testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new SimpleCarDtoAssembler(documentService, s3StorageService);
  }

  @Test
  public void toDtoFillsCommonData() {
    final Car car = setupCar();

    final SimpleCarDto result = testedInstance.toDto(Collections.singleton(car)).get(0);

    assertEquals(car.getId(), result.getId());
    assertEquals(car.getCarCategories(), result.getCategories());
    assertEquals(car.getColor(), result.getColor());
    assertEquals(car.getDriver().getId(), result.getDriverId());
    assertEquals(car.getInspectionStatus(), result.getInspectionStatus());
    assertEquals(car.getLicense(), result.getLicense());
    assertEquals(car.getModel(), result.getModel());
    assertEquals(car.getMake(), result.getMake());
    assertEquals(car.getYear(), result.getYear());
  }

  @Test
  public void toDtoFillsCarPhotoStatuses() {
    final Car car = setupCar();
    Map<DocumentType, DocumentStatus> expectedStatuses = ImmutableMap.of(
      DocumentType.CAR_PHOTO_BACK, DocumentStatus.APPROVED,
      DocumentType.CAR_PHOTO_FRONT, DocumentStatus.PENDING,
      DocumentType.CAR_PHOTO_INSIDE, DocumentStatus.REJECTED,
      DocumentType.CAR_PHOTO_TRUNK, DocumentStatus.EXPIRED
    );
    when(documentService.findCarsDocuments(anyCollectionOf(Car.class), eq(DocumentType.CAR_DOCUMENTS)))
      .thenReturn(ImmutableMap.of(
        DocumentType.CAR_PHOTO_BACK, ImmutableMap.of(1L, createDocument(expectedStatuses.get(DocumentType.CAR_PHOTO_BACK))),
        DocumentType.CAR_PHOTO_FRONT, ImmutableMap.of(1L, createDocument(expectedStatuses.get(DocumentType.CAR_PHOTO_FRONT))),
        DocumentType.CAR_PHOTO_INSIDE, ImmutableMap.of(1L, createDocument(expectedStatuses.get(DocumentType.CAR_PHOTO_INSIDE))),
        DocumentType.CAR_PHOTO_TRUNK, ImmutableMap.of(1L, createDocument(expectedStatuses.get(DocumentType.CAR_PHOTO_TRUNK)))
      ));

    final SimpleCarDto result = testedInstance.toDto(Collections.singleton(car)).get(0);

    for (Map.Entry<DocumentType, DocumentStatus> entry : expectedStatuses.entrySet()) {
      assertEquals(entry.getValue(), result.getCarPhotosStatus().get(entry.getKey()));
    }
  }

  @Test
  public void toDtoFillsDefaultInsuranceData() {
    final Car car = setupCar();
    final String signedUrl = "CDE";
    car.getDriver().setInsurancePictureUrl("ABC");
    when(s3StorageService.getSignedURL(anyString())).thenReturn(signedUrl);

    final SimpleCarDto result = testedInstance.toDto(Collections.singleton(car)).get(0);

    assertEquals(signedUrl, result.getInsurancePhotoUrl());
  }

  @Test
  public void toDtoFillsDocumentInsuranceData() {
    final Car car = setupCar();
    final String signedUrl = "CDE";
    when(s3StorageService.getSignedURL(anyString())).thenReturn(signedUrl);
    when(documentService.findCarsDocuments(anyCollectionOf(Car.class), eq(DocumentType.CAR_DOCUMENTS)))
      .thenReturn(ImmutableMap.of(
        DocumentType.INSURANCE, ImmutableMap.of(1L, createDocument(DocumentStatus.APPROVED, "ABC"))
      ));

    final SimpleCarDto result = testedInstance.toDto(Collections.singleton(car)).get(0);

    assertEquals(signedUrl, result.getInsurancePhotoUrl());
  }

  @Test
  public void toDtoFillsInsuranceStatus() {
    final Car car = setupCar();
    final DocumentStatus insuranceStatus = DocumentStatus.APPROVED;
    when(documentService.findCarsDocuments(anyCollectionOf(Car.class), eq(DocumentType.CAR_DOCUMENTS)))
      .thenReturn(ImmutableMap.of(
        DocumentType.INSURANCE, ImmutableMap.of(1L, createDocument(insuranceStatus))
      ));

    final SimpleCarDto result = testedInstance.toDto(Collections.singleton(car)).get(0);

    assertEquals(insuranceStatus, result.getInsuranceStatus());
  }

  @Test
  public void toDtoFillsInspectionStickerStatus() {
    final Car car = setupCar();
    final DocumentStatus stickerStatus = DocumentStatus.APPROVED;
    when(documentService.findCarsDocuments(anyCollectionOf(Car.class), eq(DocumentType.CAR_DOCUMENTS)))
      .thenReturn(ImmutableMap.of(
        DocumentType.CAR_STICKER, ImmutableMap.of(1L, createDocument(stickerStatus))
      ));

    final SimpleCarDto result = testedInstance.toDto(Collections.singleton(car)).get(0);

    assertEquals(stickerStatus, result.getInspectionStickerStatus());
  }

  @Test
  public void toDtoDefaultsMissingDocumentStatusToPending() {
    final Car car = setupCar();
    final DocumentStatus expected = DocumentStatus.PENDING;

    final SimpleCarDto result = testedInstance.toDto(Collections.singleton(car)).get(0);

    assertEquals(expected, result.getInspectionStickerStatus());
    assertEquals(expected, result.getInsuranceStatus());
  }

  private Document createDocument(final DocumentStatus status) {
    final Document document = new Document();
    document.setDocumentStatus(status);
    return document;
  }

  private Document createDocument(final DocumentStatus status, final String url) {
    final Document document = createDocument(status);
    document.setDocumentUrl(url);
    return document;
  }

  private Car setupCar() {
    final Car car = new Car();
    car.setId(1L);
    car.setCarCategories(Collections.singleton("REGULAR"));
    car.setColor("COLOR");
    final Driver driver = new Driver();
    driver.setId(1L);
    car.setDriver(driver);
    car.setInspectionStatus(CarInspectionStatus.APPROVED);
    car.setLicense("ASD");
    car.setModel("MODEL");
    car.setMake("MAKE");
    car.setYear("1989");
    return car;
  }
}