package com.rideaustin.service;

import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.apache.commons.mail.EmailException;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.rideaustin.config.CacheConfiguration;
import com.rideaustin.events.OnboardingUpdateEvent;
import com.rideaustin.events.TNCCardUpdateEvent;
import com.rideaustin.model.AvatarDocument;
import com.rideaustin.model.CarDocument;
import com.rideaustin.model.Document;
import com.rideaustin.model.DocumentDto;
import com.rideaustin.model.enums.CityApprovalStatus;
import com.rideaustin.model.enums.DocumentStatus;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.ride.Car;
import com.rideaustin.model.user.Avatar;
import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.dsl.AvatarDocumentDslRepository;
import com.rideaustin.repo.dsl.CarDocumentDslRepository;
import com.rideaustin.repo.dsl.DocumentDslRepository;
import com.rideaustin.repo.dsl.DriverDslRepository;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.rest.model.DriverOnboardingInfo;
import com.rideaustin.rest.model.ListAvatarDocumentsParams;
import com.rideaustin.rest.model.ListCarDocumentsParams;
import com.rideaustin.service.email.AbstractTemplateEmail;
import com.rideaustin.service.email.EmailService;
import com.rideaustin.service.thirdparty.S3StorageService;
import com.rideaustin.service.user.InsuranceUpdatedEmail;
import com.rideaustin.service.user.LicenseUpdatedEmail;
import com.rideaustin.utils.DriverUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DocumentService {

  private static final String DOCUMENT_NOT_FOUND_MESSAGE = "Document not Found";

  private final CityCache cityCache;
  private final DocumentDslRepository documentDslRepository;
  private final DriverDslRepository driverDslRepository;
  private final CarDocumentDslRepository carDocumentDslRepository;
  private final AvatarDocumentDslRepository avatarDocumentDslRepository;
  private final S3StorageService s3StorageService;
  private final EmailService emailService;
  private final CurrentUserService currentUserService;
  private final ApplicationEventPublisher publisher;
  private final List<DocumentExpirationHandler> expirationHandlers;

  public Document uploadDocument(MultipartFile file, DocumentType documentType, Date validityDate, Long cityId, Long driverId) throws ServerError {
    String documentUrl;
    if (documentType.isPrivate()) {
      documentUrl = s3StorageService.savePrivateOrThrow(documentType.getFolderName(), file);
    } else {
      documentUrl = s3StorageService.savePublicOrThrow(documentType.getDefaultName(), documentType.getFolderName(), file);
    }
    return createDocument(documentType, documentUrl, validityDate, cityId, driverId);
  }

  public Document uploadPublicDocument(MultipartFile file, DocumentType documentType, Long cityId, Long driverId) throws ServerError {
    String documentUrl = s3StorageService.savePublicOrThrow(documentType.name(), documentType.getFolderName(), file);
    return createDocument(documentType, documentUrl, null, cityId, driverId);
  }

  public Document findAvatarDocument(Driver driver, DocumentType documentType) {
    return documentDslRepository.findByAvatarAndType(driver, documentType);
  }

  public Document findAvatarDocument(Long driverId, DocumentType documentType) {
    return documentDslRepository.findByAvatarAndType(driverId, documentType);
  }

  public Document findCarDocument(Long carId, DocumentType documentType) {
    return carDocumentDslRepository.findByCarAndType(carId, documentType);
  }

  public List<DocumentDto> listCarDocuments(ListCarDocumentsParams listCarDocumentsParams) {
    return documentDslRepository.findCarDocuments(listCarDocumentsParams);
  }

  public List<DocumentDto> listAvatarDocuments(ListAvatarDocumentsParams listAvatarDocumentsParams) {
    return documentDslRepository.findAvatarDocuments(listAvatarDocumentsParams);
  }

  public Map<DocumentType, Map<Long, Document>> findAvatarsDocuments(Iterable<Driver> drivers, Collection<DocumentType> types) {
    return documentDslRepository.findDocumentsByAvatarsAndTypes(drivers, types);
  }

  public Map<DocumentType, Map<Long, Document>> findCarsDocuments(Collection<Car> cars, Collection<DocumentType> types) {
    return carDocumentDslRepository.findDocumentsByCarsAndTypes(cars, types);
  }

  @CacheEvict(cacheNames = CacheConfiguration.DOCUMENTS_CACHE, keyGenerator = CacheConfiguration.DOCUMENT_CACHE_KEY_GENERATOR)
  public void uploadAvatarDocument(MultipartFile file, DocumentType documentType, Date validityDate, Avatar avatar, Long cityId) throws ServerError {
    // criteria to locate existing non-removed docs of specified type
    ListAvatarDocumentsParams paramsExistingDocs = new ListAvatarDocumentsParams();
    paramsExistingDocs.setAvatarId(avatar.getId());
    paramsExistingDocs.setDocumentType(documentType);
    if (documentType.isCitySpecific()) { // for example, for TNC_CARD we must ensure to have a single doc per City
      paramsExistingDocs.setCityId(cityId);
    }

    // if a new TNC card is being uploaded the city approval status must be set to 'PENDING'
    if (documentType.equals(DocumentType.TNC_CARD) && avatar instanceof Driver) {
      Driver driver = (Driver) avatar;
      if (!CityApprovalStatus.PENDING.equals(driver.getCityApprovalStatus())) {
        DriverOnboardingInfo copy = DriverUtils.createCopy(driver);
        driver.setCityApprovalStatus(CityApprovalStatus.PENDING);
        this.driverDslRepository.saveAs(driver, currentUserService.getUser());
        publisher.publishEvent(new OnboardingUpdateEvent<>(copy, driver, avatar.getId()));
      }
    }

    // mark existing docs as 'removed'
    List<DocumentDto> documents = documentDslRepository.findAvatarDocuments(paramsExistingDocs);
    documentDslRepository.setRemoved(documents.stream().map(DocumentDto::getId).collect(Collectors.toSet()));

    // upload new doc
    saveAvatarDocument(avatar, uploadDocument(file, documentType, validityDate, cityId, avatar.getId()));
  }

  @CacheEvict(cacheNames = CacheConfiguration.DOCUMENTS_CACHE, keyGenerator = CacheConfiguration.DOCUMENT_CACHE_KEY_GENERATOR)
  public void uploadCarDocument(MultipartFile file, DocumentType documentType, Date validityDate, Long cityId, Driver driver, Car car) throws RideAustinException {
    // criteria to locate existing non-removed docs of specified type
    ListCarDocumentsParams paramsExistingDocs = new ListCarDocumentsParams();
    paramsExistingDocs.setDriverId(driver.getId());
    paramsExistingDocs.setCarId(car.getId());
    paramsExistingDocs.setDocumentType(documentType);

    // mark existing docs as 'removed'
    List<DocumentDto> documents = documentDslRepository.findCarDocuments(paramsExistingDocs);
    documentDslRepository.setRemoved(documents.stream().map(DocumentDto::getId).collect(Collectors.toSet()));

    // upload new doc
    saveCarDocument(car, uploadDocument(file, documentType, validityDate, cityId, driver.getId()));
  }

  @CacheEvict(cacheNames = CacheConfiguration.DOCUMENTS_CACHE, keyGenerator = CacheConfiguration.DOCUMENT_CACHE_KEY_GENERATOR)
  public void saveAvatarDocument(@Nonnull Avatar avatar, Document document) {
    AvatarDocument avatarDocument = new AvatarDocument();
    avatarDocument.setAvatar(avatar);
    avatarDocument.setDocument(document);
    documentDslRepository.saveAny(avatarDocument);
    sendDocumentChangeNotification(document);
  }

  @CacheEvict(cacheNames = CacheConfiguration.DOCUMENTS_CACHE, keyGenerator = CacheConfiguration.DOCUMENT_CACHE_KEY_GENERATOR)
  public void saveCarDocument(@Nonnull Car car, Document document) {
    CarDocument carDocument = new CarDocument();
    carDocument.setCar(car);
    carDocument.setDocument(document);
    sendDocumentChangeNotification(car, document);
    documentDslRepository.saveAny(carDocument);
  }

  public Document createDocument(DocumentType documentType, String documentUrl, Date validityDate, Long cityId, Long driverId) {
    Document document = new Document();
    document.setDocumentUrl(documentUrl);
    StringBuilder documentName = new StringBuilder(documentType.getDefaultName());
    document.setDocumentType(documentType);
    document.setValidityDate(validityDate);
    if (documentType.isCitySpecific()) {
      document.setCityId(cityId);
      documentName.append(" - ").append(cityCache.getCity(cityId).getName());
    }
    document.setName(documentName.toString());
    document.setDocumentStatus(DocumentStatus.PENDING);

    document = documentDslRepository.save(document);

    publisher.publishEvent(new OnboardingUpdateEvent<>(null, document, driverId));

    return document;
  }

  public void removeDocument(long documentId) throws NotFoundException {
    Document document = Optional.ofNullable(documentDslRepository.findOne(documentId)).orElseThrow(() -> new NotFoundException(DOCUMENT_NOT_FOUND_MESSAGE));
    document.setRemoved(Boolean.TRUE);
    documentDslRepository.save(document);
  }

  public Document updateDocument(Long documentId, Document document) throws NotFoundException {
    Document existing = Optional.ofNullable(documentDslRepository.findOne(documentId)).orElseThrow(() -> new NotFoundException(DOCUMENT_NOT_FOUND_MESSAGE));
    Document copy = new Document();
    BeanUtils.copyProperties(existing, copy);
    existing.setDocumentStatus(document.getDocumentStatus());
    Optional.ofNullable(document.getValidityDate()).ifPresent(existing::setValidityDate);
    if (existing.getDocumentStatus() == DocumentStatus.EXPIRED && existing.isValidNow()) {
      existing.setDocumentStatus(DocumentStatus.PENDING);
    }
    Document savedDocument = documentDslRepository.save(existing);
    sendDocumentChangeNotification(savedDocument);
    publishDocumentUpdateEvents(copy, savedDocument, documentDslRepository.findDriver(savedDocument).getId());
    return savedDocument;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void updatedExpiredDocument(Long documentId) {
    Document document = documentDslRepository.findOne(documentId);
    Driver driver = null;
    Long driverId = null;
    if (DocumentType.DRIVER_DOCUMENTS.contains(document.getDocumentType())) {
      driverId = avatarDocumentDslRepository.findOwnerIdByDocumentId(documentId);
    } else {
      driver = carDocumentDslRepository.findOwnerByDocumentId(documentId);
    }
    Document copy = new Document();
    BeanUtils.copyProperties(document, copy);
    document.setDocumentStatus(DocumentStatus.EXPIRED);
    for (DocumentExpirationHandler expirationHandler : expirationHandlers) {
      if (expirationHandler.supports(document.getDocumentType())) {
        expirationHandler.handle(document);
      }
    }
    this.documentDslRepository.save(document);
    if (driver != null) {
      publisher.publishEvent(new OnboardingUpdateEvent<>(copy, document, driver.getId()));
    } else if (driverId != null) {
      publisher.publishEvent(new OnboardingUpdateEvent<>(copy, document, driverId));
    }
  }

  private void publishDocumentUpdateEvents(Document oldDocument, Document newDocument, Long driverId) {
    if (oldDocument.getDocumentStatus() != newDocument.getDocumentStatus() && newDocument.getDocumentType() == DocumentType.TNC_CARD) {
      publisher.publishEvent(new TNCCardUpdateEvent(newDocument, newDocument.getDocumentStatus()));
    }
    publisher.publishEvent(new OnboardingUpdateEvent<>(oldDocument, newDocument, driverId));
  }

  private void sendDocumentChangeNotification(Document document) {
    Set<DocumentType> notifiedTypes = EnumSet.of(DocumentType.LICENSE, DocumentType.INSURANCE);
    if (!notifiedTypes.contains(document.getDocumentType()) || currentUserService.getUser().isAdmin()) {
      return;
    }
    Car car = documentDslRepository.findCar(document);
    sendDocumentChangeNotification(car, document);
  }

  private void sendDocumentChangeNotification(Car car, Document document) {
    Driver driver;
    if (car == null) {
      driver = documentDslRepository.findDriver(document);
    } else {
      driver = car.getDriver();
    }
    try {
      Optional<AbstractTemplateEmail> email = resolveEmail(driver, car, document);
      if (email.isPresent()) {
        emailService.sendEmail(email.get());
      }
    } catch (EmailException ex) {
      log.error("Unable to send document update notification email", ex);
    }
  }

  private Optional<AbstractTemplateEmail> resolveEmail(Driver driver, Car car, Document document) throws EmailException {
    if (document.getDocumentType() == DocumentType.LICENSE) {
      return Optional.of(new LicenseUpdatedEmail(driver, cityCache.getCity(driver.getCityId())));
    } else if (document.getDocumentType() == DocumentType.INSURANCE && car != null) {
      return Optional.of(new InsuranceUpdatedEmail(driver, car, cityCache.getCity(driver.getCityId())));
    }
    return Optional.empty();
  }
}
