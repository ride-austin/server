package com.rideaustin.assemblers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.rideaustin.model.Document;
import com.rideaustin.model.enums.CityApprovalStatus;
import com.rideaustin.model.enums.DocumentStatus;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.enums.DriverActivationStatus;
import com.rideaustin.model.enums.DriverOnboardingStatus;
import com.rideaustin.model.enums.PayoneerStatus;
import com.rideaustin.model.ride.Car;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.Gender;
import com.rideaustin.model.user.User;
import com.rideaustin.rest.model.SimpleCarDto;
import com.rideaustin.rest.model.SimpleDriverDto;
import com.rideaustin.service.DocumentService;
import com.rideaustin.service.thirdparty.S3StorageService;
import com.rideaustin.service.user.DriverTypeCache;
import com.rideaustin.service.user.DriverTypeUtils;

public class SimpleDriverDtoAssemblerTest {

  @Mock
  private DocumentService documentService;
  @Mock
  private S3StorageService s3StorageService;
  @Mock
  private SimpleCarDtoAssembler carDtoAssembler;
  @Mock
  private DriverTypeCache driverTypeCache;

  private SimpleDriverDtoAssembler testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    DriverTypeUtils.setDriverTypeCache(driverTypeCache);

    testedInstance = new SimpleDriverDtoAssembler(s3StorageService, documentService, carDtoAssembler);
  }

  @Test
  public void toDtoFillsCommonData() {
    final Driver source = new Driver();
    source.setActive(true);
    source.setCityApprovalStatus(CityApprovalStatus.APPROVED);
    source.setActivationStatus(DriverActivationStatus.ACTIVE);
    source.setId(1L);
    source.setPayoneerStatus(PayoneerStatus.ACTIVE);
    source.setRating(5.0);
    source.setOnboardingStatus(DriverOnboardingStatus.ACTIVE);
    source.setLastLoginDate(new Date());
    final User user = new User();
    user.setEmail("asd@fgd.ee");
    user.setUserEnabled(true);
    user.setFirstname("A");
    user.setGender(Gender.FEMALE);
    user.setLastname("V");
    user.setPhoneNumber("+1512386161");
    user.setId(2L);
    source.setUser(user);

    final SimpleDriverDto result = testedInstance.toDto(Collections.singletonList(source)).get(0);

    assertEquals(source.isActive(), result.getActive());
    assertEquals(source.getCityApprovalStatus(), result.getCityApprovalStatus());
    assertEquals(source.getActivationStatus(), result.getActivationStatus());
    assertEquals(source.getId(), result.getDriverId());
    assertEquals(source.getPayoneerStatus(), result.getPayoneerStatus());
    assertEquals(source.getRating(), result.getRating());
    assertEquals(source.getLastLoginDate(), result.getLastLoginDate());
    assertEquals(source.getEmail(), result.getEmail());
    assertEquals(source.getUser().getUserEnabled(), result.getEnabled());
    assertEquals(source.getFirstname(), result.getFirstName());
    assertEquals(source.getGender(), result.getGender());
    assertEquals(source.getLastname(), result.getLastName());
    assertEquals(source.getPhoneNumber(), result.getPhoneNumber());
    assertEquals(source.getUser().getId(), result.getUserId());

  }

  @Test
  public void toDtoFillsCarInfo() {
    Driver source = new Driver();
    source.setUser(new User());
    final long activeCarId = 1L;
    final long removedCarId = 2L;
    final Car activeCar = new Car();
    activeCar.setId(activeCarId);
    final Car removedCar = new Car();
    removedCar.setId(removedCarId);
    source.setCars(ImmutableSet.of(activeCar, removedCar));
    when(carDtoAssembler.toDto(anySetOf(Car.class))).thenAnswer((Answer<List<SimpleCarDto>>) invocation -> {
      List<SimpleCarDto> result = new ArrayList<>();
      for (int i = 0; i < invocation.getArguments().length; i++) {
        final Collection<Car> cars = (Collection<Car>) invocation.getArguments()[i];
        result.add(new SimpleCarDto.Builder()
          .id(cars.iterator().next().getId())
          .driverId(1L)
          .build());
      }
      return result;
    });

    final SimpleDriverDto result = testedInstance.toDto(Collections.singletonList(source)).get(0);

    assertEquals(1, result.getCars().size());
    assertEquals(activeCarId, result.getCars().get(0).getId());
  }

  @Test
  public void toDtoSetsLicenseSignedUrlIfExists() {
    Driver source = new Driver();
    source.setId(1L);
    source.setUser(new User());
    final Document license = new Document();
    final String url = "url";
    final String signedUrl = "signed";
    license.setDocumentUrl(url);
    when(documentService.findAvatarsDocuments(anyListOf(Driver.class), eq(DocumentType.DRIVER_DOCUMENTS)))
      .thenReturn(ImmutableMap.of(
        DocumentType.LICENSE, ImmutableMap.of(1L, license)
      ));
    when(s3StorageService.getSignedURL(url)).thenReturn(signedUrl);

    final SimpleDriverDto result = testedInstance.toDto(Collections.singletonList(source)).get(0);

    assertEquals(signedUrl, result.getDriverLicensePicture());
  }

  @Test
  public void toDtoSetsLicenseStatus() {
    Driver source = new Driver();
    source.setId(1L);
    source.setUser(new User());
    final Document license = new Document();
    license.setDocumentStatus(DocumentStatus.PENDING);
    when(documentService.findAvatarsDocuments(anyListOf(Driver.class), eq(DocumentType.DRIVER_DOCUMENTS)))
      .thenReturn(ImmutableMap.of(
        DocumentType.LICENSE, ImmutableMap.of(1L, license)
      ));

    final SimpleDriverDto result = testedInstance.toDto(Collections.singletonList(source)).get(0);

    assertEquals(DocumentStatus.PENDING, result.getDriverLicenseStatus());
  }

  @Test
  public void toDtoSetsDriverPicture() {
    Driver source = new Driver();
    source.setId(1L);
    source.setUser(new User());
    final Document driverPhoto = new Document();
    final String url = "url";
    driverPhoto.setDocumentUrl(url);
    when(documentService.findAvatarsDocuments(anyListOf(Driver.class), eq(DocumentType.DRIVER_DOCUMENTS)))
      .thenReturn(ImmutableMap.of(
        DocumentType.DRIVER_PHOTO, ImmutableMap.of(1L, driverPhoto)
      ));

    final SimpleDriverDto result = testedInstance.toDto(Collections.singletonList(source)).get(0);

    assertEquals(url, result.getDriverPicture());
  }

  @Test
  public void toDtoSetsDriverPhotoStatus() {
    Driver source = new Driver();
    source.setId(1L);
    source.setUser(new User());
    final Document driverPhoto = new Document();
    driverPhoto.setDocumentStatus(DocumentStatus.PENDING);
    when(documentService.findAvatarsDocuments(anyListOf(Driver.class), eq(DocumentType.DRIVER_DOCUMENTS)))
      .thenReturn(ImmutableMap.of(
        DocumentType.DRIVER_PHOTO, ImmutableMap.of(1L, driverPhoto)
      ));

    final SimpleDriverDto result = testedInstance.toDto(Collections.singletonList(source)).get(0);

    assertEquals(DocumentStatus.PENDING, result.getProfilePhotosStatus());
  }

  @Test
  public void toDtoSetsDriverTypes() {
    Driver source = new Driver();
    source.setGrantedDriverTypesBitmask(1);
    source.setUser(new User());
    final Set<String> driverTypes = Collections.singleton("DIRECT_CONNECT");
    when(driverTypeCache.fromBitMask(1)).thenReturn(driverTypes);

    final SimpleDriverDto result = testedInstance.toDto(Collections.singletonList(source)).get(0);

    assertTrue(CollectionUtils.isEqualCollection(driverTypes, result.getDriverTypes()));
  }
}