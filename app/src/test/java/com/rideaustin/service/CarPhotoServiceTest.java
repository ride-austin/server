package com.rideaustin.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.rideaustin.model.Document;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.ride.Car;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.CarDocumentDslRepository;
import com.rideaustin.repo.dsl.CarDslRepository;
import com.rideaustin.repo.dsl.DocumentDslRepository;
import com.rideaustin.rest.exception.ForbiddenException;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.service.thirdparty.S3StorageService;
import com.rideaustin.service.user.CarPhotoService;

@RunWith(MockitoJUnitRunner.class)
public class CarPhotoServiceTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private DocumentDslRepository documentDslRepository;
  @Mock
  private CarDocumentDslRepository carDocumentDslRepository;
  @Mock
  private CarDslRepository carDslRepository;
  @Mock
  private S3StorageService s3StorageService;
  @Mock
  private CurrentUserService currentUserService;
  @Mock
  private CommonsMultipartFile commonsMultipartFile;
  @Mock
  private DocumentService documentService;

  private CarPhotoService carPhotoService;

  private User user = new User();

  private Car car = new Car();

  @Before
  public void setup() throws Exception {

    carPhotoService = new CarPhotoService(documentDslRepository, carDocumentDslRepository, documentService, carDslRepository,
      s3StorageService, currentUserService);

    user = new User();
    user.setId(1L);

    car = new Car();
    car.setId(1L);

    Driver driver = new Driver();
    driver.setUser(user);
    car.setDriver(driver);
  }

  @Test
  public void testSaveNewCarPhoto() throws Exception {

    when(currentUserService.getUser()).thenReturn(user);
    when(carDslRepository.findOne(1L)).thenReturn(car);
    when(s3StorageService.savePublicOrThrow(anyString(), anyString(), any(MultipartFile.class))).thenReturn("url");

    Document carPhoto = carPhotoService.saveNewCarPhoto(1L, DocumentType.CAR_PHOTO_FRONT, commonsMultipartFile);

    assertThat(carPhoto.getDocumentUrl(), is("url"));
    assertThat(carPhoto.getDocumentType(), is(DocumentType.CAR_PHOTO_FRONT));
  }

  @Test
  public void testSaveNewCarPhotoCarNotFound() throws Exception {

    when(currentUserService.getUser()).thenReturn(user);
    when(carDslRepository.findOne(1L)).thenReturn(null);
    when(s3StorageService.savePublicOrThrow(anyString(), anyString(), any(MultipartFile.class))).thenReturn("url");

    expectedException.expect(NotFoundException.class);
    expectedException.expectMessage("Car not found");

    carPhotoService.saveNewCarPhoto(1L, DocumentType.CAR_PHOTO_FRONT, commonsMultipartFile);
  }

  @Test
  public void testSaveNewCarPhotoWrongUser() throws Exception {

    user = new User();
    user.setId(10L);

    car = new Car();
    car.setId(1L);

    User carUser = new User();
    carUser.setId(2L);

    Driver driver = new Driver();
    driver.setUser(carUser);
    car.setDriver(driver);

    when(currentUserService.getUser()).thenReturn(user);
    when(carDslRepository.findOne(1L)).thenReturn(car);
    when(s3StorageService.savePublicOrThrow(anyString(), anyString(), any(MultipartFile.class))).thenReturn("url");

    expectedException.expect(ForbiddenException.class);
    expectedException.expectMessage("User is not allowed to change other driver's car photo");

    carPhotoService.saveNewCarPhoto(1L, DocumentType.CAR_PHOTO_FRONT, commonsMultipartFile);
  }

  @Test
  public void testSaveNewCarPhotoWrongUserButAdmin() throws Exception {

    user = new User();
    user.setId(1L);
    user.getAvatarTypes().add(AvatarType.ADMIN);
    car = new Car();
    car.setId(1L);

    User carUser = new User();
    carUser.setId(2L);

    Driver driver = new Driver();
    driver.setUser(carUser);
    car.setDriver(driver);

    when(currentUserService.getUser()).thenReturn(user);
    when(carDslRepository.findOne(1L)).thenReturn(car);
    when(s3StorageService.savePublicOrThrow(anyString(), anyString(), any(MultipartFile.class))).thenReturn("url");

    Document newCarPhoto = carPhotoService.saveNewCarPhoto(1L, DocumentType.CAR_PHOTO_FRONT, commonsMultipartFile);

    assertThat(newCarPhoto.getDocumentUrl(), is("url"));
    assertThat(newCarPhoto.getDocumentType(), is(DocumentType.CAR_PHOTO_FRONT));
  }

}