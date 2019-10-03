package com.rideaustin.service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import org.apache.commons.mail.EmailException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.rideaustin.events.LostAndFoundTrackEvent;
import com.rideaustin.model.City;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.LostAndFoundRequestType;
import com.rideaustin.model.lostandfound.LostAndFoundRequestDto;
import com.rideaustin.model.lostandfound.LostItemInfo;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.dsl.LostAndFoundDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.exception.ForbiddenException;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.email.EmailService;
import com.rideaustin.service.email.FoundItemEmail;
import com.rideaustin.service.email.LostItemEmail;
import com.rideaustin.service.thirdparty.CommunicationService;
import com.rideaustin.service.thirdparty.CommunicationServiceFactory;
import com.rideaustin.service.thirdparty.S3StorageService;
import com.rideaustin.service.thirdparty.StorageItem;
import com.rideaustin.service.user.LostAndFoundFallbackSMS;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LostAndFoundService {

  private final CommunicationService communicationService;
  private final EmailService emailService;
  private final CityService cityService;
  private final S3StorageService s3StorageService;
  private final DriverService driverService;
  private final CurrentUserService currentUserService;
  private final RideDslRepository rideDslRepository;
  private final LostAndFoundDslRepository repository;
  private final ApplicationEventPublisher publisher;
  private final int expirationHours;

  private static final String FAILED_TO_SEND_EMAIL_MESSAGE = "Failed to send email";

  @Inject
  public LostAndFoundService(CommunicationServiceFactory serviceFactory, EmailService emailService,
    CityService cityService, S3StorageService s3StorageService, DriverService driverService, Environment environment,
    CurrentUserService currentUserService, RideDslRepository rideDslRepository, LostAndFoundDslRepository repository, ApplicationEventPublisher publisher) {
    this.communicationService = serviceFactory.createCommunicationService();
    this.emailService = emailService;
    this.cityService = cityService;
    this.s3StorageService = s3StorageService;
    this.driverService = driverService;
    this.currentUserService = currentUserService;
    this.rideDslRepository = rideDslRepository;
    this.repository = repository;
    this.publisher = publisher;

    this.expirationHours = environment.getProperty("found.item.photo.expiration", Integer.class, 120);
  }

  @Transactional
  public CommunicationService.CallStatus initiateCall(long rideId, String phoneNumber) throws RideAustinException {
    CommunicationService.CallStatus callStatus = CommunicationService.CallStatus.ERROR;
    LostItemInfo lostItemInfo = null;
    try {
      Ride ride = rideDslRepository.findOne(rideId);
      if (ride == null) {
        throw new NotFoundException("Ride is not found");
      }
      callStatus = communicationService.callParticipant(ride, AvatarType.RIDER, phoneNumber);
      lostItemInfo = doProcessLostItem(rideId, "Not provided", "Rider initiated a call to driver", phoneNumber, false);
      publishEvent(lostItemInfo.getRiderId(), LostAndFoundRequestType.CALL, "Rider initiated a call to driver");
    } catch (EmailException ex) {
      log.error(FAILED_TO_SEND_EMAIL_MESSAGE, ex);
    }
    if (callStatus == CommunicationService.CallStatus.ERROR && lostItemInfo != null) {
      communicationService.sendSms(new LostAndFoundFallbackSMS(phoneNumber, lostItemInfo.getDriverPhone()));
    }
    return callStatus;
  }

  public void processLostItem(Long rideId, String description, String details, String phoneNumber) throws RideAustinException {
    try {
      LostItemInfo lostItemInfo = doProcessLostItem(rideId, description, details, phoneNumber, true);
      publishEvent(lostItemInfo.getRiderId(), LostAndFoundRequestType.EMAIL, description);
    } catch (EmailException ex) {
      log.error(FAILED_TO_SEND_EMAIL_MESSAGE, ex);
    }
  }

  public void processFoundItem(Long rideId, Date foundOn, String rideDescription, String details, Boolean sharingContactsAllowed,
    MultipartFile itemPhoto) throws RideAustinException {
    try {
      String url = null;
      if (itemPhoto != null) {
        url = uploadPhoto(itemPhoto);
      }
      Driver driver = driverService.getCurrentDriver();
      City city = cityService.getById(driver.getCityId());
      publishEvent(driver.getId(), LostAndFoundRequestType.EMAIL, details);
      emailService.sendEmail(new FoundItemEmail(rideId, driver, foundOn, rideDescription, details, sharingContactsAllowed, url, city));
    } catch (EmailException ex) {
      log.error(FAILED_TO_SEND_EMAIL_MESSAGE, ex);
    }
  }

  private String uploadPhoto(MultipartFile itemPhoto) {
    try {
      StorageItem storageItem = StorageItem.builder()
        .setContent(itemPhoto.getBytes())
        .setExpirationHours(expirationHours)
        .setMimeType("image/jpeg")
        .setPublicAccess(true)
        .build();

      String key = s3StorageService.uploadStorageItem(storageItem);
      return s3StorageService.getUnsignedURL(key);
    } catch (Exception e) {
      log.error("Failed to upload found item photo", e);
    }
    return null;
  }

  public List<LostAndFoundRequestDto> findRequests(Long avatarId) {
    return repository.findRequests(avatarId);
  }

  private LostItemInfo doProcessLostItem(Long rideId, String description, String details, String phoneNumber, boolean validateRider) throws NotFoundException, ForbiddenException, EmailException {
    LostItemInfo lostItemInfo = repository.getLostItemInfo(rideId);
    if (lostItemInfo == null) {
      throw new NotFoundException("Ride not found");
    }
    if (validateRider && !Objects.equals(currentUserService.getUser().getId(), lostItemInfo.getRiderUserId())) {
      throw new ForbiddenException("Rider didn't take that ride");
    }
    City city = cityService.getById(lostItemInfo.getCityId());
    emailService.sendEmail(new LostItemEmail(lostItemInfo, description, details, phoneNumber, city));
    return lostItemInfo;
  }

  private void publishEvent(Long avatarId, LostAndFoundRequestType type, String content) {
    publisher.publishEvent(new LostAndFoundTrackEvent(type, avatarId, content));
  }
}
