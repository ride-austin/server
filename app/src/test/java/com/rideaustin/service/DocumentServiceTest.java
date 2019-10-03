package com.rideaustin.service;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.multipart.MultipartFile;

import com.rideaustin.events.OnboardingUpdateEvent;
import com.rideaustin.model.City;
import com.rideaustin.model.Document;
import com.rideaustin.model.enums.CityApprovalStatus;
import com.rideaustin.model.enums.DocumentStatus;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.ride.Car;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.AvatarDocumentDslRepository;
import com.rideaustin.repo.dsl.CarDocumentDslRepository;
import com.rideaustin.repo.dsl.DocumentDslRepository;
import com.rideaustin.repo.dsl.DriverDslRepository;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.rest.model.ListAvatarDocumentsParams;
import com.rideaustin.rest.model.ListCarDocumentsParams;
import com.rideaustin.service.email.EmailService;
import com.rideaustin.service.thirdparty.S3StorageService;

public class DocumentServiceTest {

  private static final long CITY_ID = 1L;
  private static final String URL = "url";
  private static final String CITY_NAME = "name";
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private CityCache cityCache;
  @Mock
  private DocumentDslRepository documentDslRepository;
  @Mock
  private DriverDslRepository driverDslRepository;
  @Mock
  private CarDocumentDslRepository carDocumentDslRepository;
  @Mock
  private AvatarDocumentDslRepository avatarDocumentDslRepository;
  @Mock
  private S3StorageService s3StorageService;
  @Mock
  private EmailService emailService;
  @Mock
  private CurrentUserService currentUserService;
  @Mock
  private ApplicationEventPublisher publisher;
  @Mock
  private DocumentExpirationHandler expirationHandler;

  @Mock
  private User user;

  private DocumentService testedInstance;

  private MultipartFile file;

  @Before
  public void setupTests() throws ServerError {
    MockitoAnnotations.initMocks(this);

    testedInstance = new DocumentService(cityCache, documentDslRepository, driverDslRepository, carDocumentDslRepository,
      avatarDocumentDslRepository, s3StorageService, emailService, currentUserService, publisher, Collections.singletonList(expirationHandler));

    when(s3StorageService.savePrivateOrThrow(any(), any(MultipartFile.class))).thenReturn(URL);
    when(documentDslRepository.save(any(Document.class))).thenAnswer(a -> a.getArguments()[0]);
    when(cityCache.getCity(CITY_ID)).thenReturn(mockCity());
    when(currentUserService.getUser()).thenReturn(user);
    file = mock(MultipartFile.class);
  }

  @Test
  public void testUploadDocumentsS3Exception() throws ServerError {
    expectedException.expect(ServerError.class);

    when(s3StorageService.savePrivateOrThrow(any(), any(MultipartFile.class))).thenThrow(new ServerError(""));
    testedInstance.uploadDocument(file, DocumentType.CAR_STICKER, null, CITY_ID, 1L);
  }

  @Test
  public void testUploadDocumentsNonCitySpecific() throws ServerError {
    Document document = testedInstance.uploadDocument(file, DocumentType.CAR_STICKER, null, CITY_ID, 1L);

    assertThat(document.getCityId(), is(nullValue()));
    assertThat(document.getDocumentType(), is(DocumentType.CAR_STICKER));
    assertThat(document.getName(), is(not(nullValue())));
    assertThat(document.getDocumentUrl(), is(URL));
    verify(documentDslRepository, times(1)).save(any());
  }

  @Test
  public void testUploadDocumentsCitySpecific() throws ServerError {
    Document document = testedInstance.uploadDocument(file, DocumentType.TNC_CARD, new Date(), CITY_ID, 1L);

    assertThat(document.getCityId(), is(CITY_ID));
    assertThat(document.getDocumentType(), is(DocumentType.TNC_CARD));
    assertThat(document.getName(), containsString(CITY_NAME));
    assertThat(document.getDocumentUrl(), is(URL));
    verify(documentDslRepository, times(1)).save(any());
  }

  @Test
  public void testUpdateValidityDateOnExpiredDocument() throws NotFoundException {
    when(documentDslRepository.findOne(anyLong())).thenReturn(Document.builder().documentStatus(DocumentStatus.EXPIRED).build());
    when(documentDslRepository.findDriver(any(Document.class))).thenReturn(new Driver());
    final Date validityDate = Date.from(Instant.now().plus(500, ChronoUnit.DAYS));
    final Document newDocument = Document.builder().validityDate(validityDate).documentStatus(DocumentStatus.EXPIRED).build();

    final Document actual = testedInstance.updateDocument(1L, newDocument);

    assertEquals(validityDate, actual.getValidityDate());
    assertEquals(DocumentStatus.PENDING, actual.getDocumentStatus());
  }

  @Test
  public void uploadPublicDocumentSavesS3File() throws ServerError {
    final Document result = testedInstance.uploadPublicDocument(file, DocumentType.LICENSE, 1L, 1L);

    assertNotNull(result);
    verify(s3StorageService).savePublicOrThrow(DocumentType.LICENSE.name(), DocumentType.LICENSE.getFolderName(), file);
  }

  @Test
  public void findAvatarDocumentDelegatesCallToRepository() {
    final Driver driver = new Driver();
    final DocumentType type = DocumentType.LICENSE;

    testedInstance.findAvatarDocument(driver, type);

    verify(documentDslRepository).findByAvatarAndType(driver, type);
  }

  @Test
  public void findAvatarDocumentDelegatesCallToRepository_1() {
    final long avatarId = 1L;
    final DocumentType type = DocumentType.LICENSE;

    testedInstance.findAvatarDocument(avatarId, type);

    verify(documentDslRepository).findByAvatarAndType(avatarId, type);
  }

  @Test
  public void findCarDocumentDelegatesCallToRepository() {
    final long carId = 1L;
    final DocumentType type = DocumentType.INSURANCE;

    testedInstance.findCarDocument(carId, type);

    verify(carDocumentDslRepository).findByCarAndType(carId, type);
  }

  @Test
  public void listCarDocumentsDelegatesCallToRepository() {
    final ListCarDocumentsParams params = new ListCarDocumentsParams();

    testedInstance.listCarDocuments(params);

    verify(documentDslRepository).findCarDocuments(params);
  }

  @Test
  public void listAvatarDocumentsDelegatesCallToRepository() {
    final ListAvatarDocumentsParams params = new ListAvatarDocumentsParams();

    testedInstance.listAvatarDocuments(params);

    verify(documentDslRepository).findAvatarDocuments(params);
  }

  @Test
  public void findAvatarsDocumentsDelegatesCallToRepository() {
    final Set<Driver> drivers = Collections.singleton(new Driver());
    final Set<DocumentType> types = Collections.singleton(DocumentType.LICENSE);

    testedInstance.findAvatarsDocuments(drivers, types);

    verify(documentDslRepository).findDocumentsByAvatarsAndTypes(drivers, types);
  }

  @Test
  public void findCarsDocumentsDelegatesCallToRepository() {
    final List<Car> cars = Collections.singletonList(new Car());
    final Set<DocumentType> types = Collections.singleton(DocumentType.LICENSE);

    testedInstance.findCarsDocuments(cars, types);

    verify(carDocumentDslRepository).findDocumentsByCarsAndTypes(cars, types);
  }

  @Test
  public void uploadAvatarDocumentSavesDocument() throws ServerError {
    final Driver driver = new Driver();
    driver.setCityApprovalStatus(CityApprovalStatus.APPROVED);
    testedInstance.uploadAvatarDocument(file, DocumentType.TNC_CARD, new Date(), driver, 1L);

    verify(publisher, times(2)).publishEvent(any(OnboardingUpdateEvent.class));
    verify(documentDslRepository).saveAny(any(Document.class));
  }

  @Test
  public void uploadCarDocumentSavesDocument() throws RideAustinException {
    testedInstance.uploadCarDocument(file, DocumentType.CAR_STICKER, new Date(), 1L, new Driver(), new Car());

    verify(documentDslRepository).saveAny(any(Document.class));
  }

  @Test
  public void updatedExpiredDocumentLicenseCallsExpirationHandler() {
    final long documentId = 1L;
    final Document document = new Document();
    document.setDocumentType(DocumentType.LICENSE);
    when(documentDslRepository.findOne(documentId)).thenReturn(document);
    when(expirationHandler.supports(DocumentType.LICENSE)).thenReturn(true);

    testedInstance.updatedExpiredDocument(documentId);

    verify(expirationHandler).handle(document);
    verify(publisher).publishEvent(any(OnboardingUpdateEvent.class));
  }

  @Test
  public void updatedExpiredDocumentInsuranceCallsExpirationHandler() {
    final long documentId = 1L;
    final Document document = new Document();
    document.setDocumentType(DocumentType.INSURANCE);
    when(documentDslRepository.findOne(documentId)).thenReturn(document);
    when(expirationHandler.supports(DocumentType.INSURANCE)).thenReturn(true);
    when(carDocumentDslRepository.findOwnerByDocumentId(documentId)).thenReturn(new Driver());

    testedInstance.updatedExpiredDocument(documentId);

    verify(expirationHandler).handle(document);
    verify(publisher).publishEvent(any(OnboardingUpdateEvent.class));
  }

  private City mockCity() {
    City c = new City();
    c.setId(CITY_ID);
    c.setName(CITY_NAME);
    return c;
  }

}