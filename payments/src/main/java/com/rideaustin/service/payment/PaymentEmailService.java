package com.rideaustin.service.payment;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.mail.EmailException;
import org.joda.money.MoneyUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.rideaustin.Constants;
import com.rideaustin.assemblers.FarePaymentDtoEmailAssembler;
import com.rideaustin.model.City;
import com.rideaustin.model.Document;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.promocodes.Promocode;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.splitfare.FarePayment;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.RiderCard;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.CampaignService;
import com.rideaustin.service.CityCache;
import com.rideaustin.service.DocumentService;
import com.rideaustin.service.RideTrackerService;
import com.rideaustin.service.email.AbstractPaymentTemplateEmail;
import com.rideaustin.service.email.EmailService;
import com.rideaustin.service.model.CampaignDto;
import com.rideaustin.service.model.FarePaymentDto;
import com.rideaustin.service.ride.EndRideEmail;
import com.rideaustin.service.ride.RideCancellationEmail;
import com.rideaustin.service.thirdparty.S3StorageService;
import com.rideaustin.service.user.UserCardIsLockedEmail;
import com.rideaustin.service.user.UserCardIsUnlockedEmail;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PaymentEmailService {

  private final EmailService emailService;
  private final FarePaymentDtoEmailAssembler farePaymentDtoAssembler;
  private final DocumentService documentService;
  private final CityCache cityCache;
  private final S3StorageService s3StorageService;
  private final RideTrackerService rideTrackerService;
  private final CampaignService campaignService;
  private final String rideReceiptBcc;
  private final String noRideImageKey;

  @Inject
  public PaymentEmailService(EmailService emailService, FarePaymentDtoEmailAssembler farePaymentDtoAssembler,
    DocumentService documentService, CityCache cityCache, Environment environment, S3StorageService s3StorageService,
    RideTrackerService rideTrackerService, CampaignService campaignService) {
    this.emailService = emailService;
    this.farePaymentDtoAssembler = farePaymentDtoAssembler;
    this.documentService = documentService;
    this.cityCache = cityCache;
    this.s3StorageService = s3StorageService;
    this.rideTrackerService = rideTrackerService;
    this.campaignService = campaignService;

    this.rideReceiptBcc = environment.getProperty("ride.receipt.email.bcc");
    this.noRideImageKey = environment.getProperty("ride.receipt.email.no_ride_image_s3key");
  }

  public void sendEndRideParticipantEmail(Ride ride, FarePayment participantPayment) throws RideAustinException {
    try {
      byte[] imageData = getImageData(ride);
      EndRideEmail endRideEmail = new EndRideEmail(ride, participantPayment.getRider(), cityCache.getCity(ride.getCityId()),
        farePaymentDtoAssembler.toDto(participantPayment), imageData, getDriverPhoto(ride), createCampaignInfo(ride));
      endRideEmail.addRecipients(rideReceiptBcc);
      emailService.sendEmail(endRideEmail);
    } catch (EmailException e) {
      log.error("Unable to send receipt email", e);
      throw new ServerError(e);
    }

  }

  public void sendLockCardEmail(Ride ride) throws RideAustinException {
    try {
      UserCardIsLockedEmail endRideEmail = new UserCardIsLockedEmail(ride.getRider().getUser(), cityCache.getCity(ride.getCityId()));
      endRideEmail.addRecipients(rideReceiptBcc);
      emailService.sendEmail(endRideEmail);
    } catch (EmailException e) {
      log.error("Unable to send card lock email", e);
      throw new ServerError(e);
    }
  }

  public void sendUnlockCardEmail(Ride ride) throws RideAustinException {
    try {
      UserCardIsUnlockedEmail endRideEmail = new UserCardIsUnlockedEmail(ride.getRider().getUser(), cityCache.getCity(ride.getCityId()));
      endRideEmail.addRecipients(rideReceiptBcc);
      emailService.sendEmail(endRideEmail);
    } catch (EmailException e) {
      log.error("Unable to send card unlock email", e);
      throw new ServerError(e);
    }
  }

  public void sendEndRideEmail(Ride ride, FarePayment mainRiderPayment, List<FarePayment> participantsPayment,
    Promocode promocode) throws ServerError {
    try {
      byte[] imageData = getImageData(ride);
      AbstractPaymentTemplateEmail paymentEmail = null;
      City city = cityCache.getCity(ride.getCityId());
      FarePaymentDto riderPayment = farePaymentDtoAssembler.toDto(mainRiderPayment);
      if (ride.getStatus().equals(RideStatus.COMPLETED)) {
        paymentEmail = new EndRideEmail(ride, ride.getRider(), city,
          riderPayment, farePaymentDtoAssembler.toDto(participantsPayment),
          imageData, promocode, getDriverPhoto(ride), createCampaignInfo(ride));
      } else if (ride.getCancellationFee() != null) {
        paymentEmail = new RideCancellationEmail(ride, imageData, city, riderPayment);
      }
      if (paymentEmail != null) {
        paymentEmail.addRecipients(rideReceiptBcc);
        emailService.sendEmail(paymentEmail);
      }
    } catch (EmailException e) {
      log.error("Unable to send receipt email", e);
      throw new ServerError(e);
    }
  }

  private CampaignDto createCampaignInfo(Ride ride) {
    return campaignService.findExistingCampaignForRide(ride)
      .map(c -> {
        if (ride.getStripeCreditCharge().equals(Constants.ZERO_USD)) {
          return new CampaignDto(c.getReceiptTitle(), ride.getTotalCharge(), c.getReceiptImage());
        } else {
          return new CampaignDto(c.getReceiptTitle(), MoneyUtils.min(c.getCappedAmount(), ride.getTotalCharge()),
            c.getReceiptImage());
        }
      })
      .orElse(null);
  }

  public void notifyRiderInvalidPayment(Ride ride, Rider rider, RiderCard card) throws RideAustinException {
    try {
      InvalidPaymentEmail invalidPaymentEmail = new InvalidPaymentEmail(ride, card, cityCache.getCity(ride.getCityId()));
      invalidPaymentEmail.addRecipients(rider.getEmail());
      emailService.sendEmail(invalidPaymentEmail);
    } catch (EmailException e) {
      log.error("Unable to send invalid payment email", e);
      throw new ServerError(e);
    }
  }

  private byte[] getImageData(Ride ride) throws EmailException {
    byte[] imageData = null;
    try {
      if (ride.getRideMap() != null) {
        imageData = s3StorageService.loadFile(ride.getRideMap());
      } else {
        // attempt to recreate map if null
        imageData = rideTrackerService.saveStaticImage(ride);
      }
    } catch (IOException e) {
      log.warn("Failed to load ride map. Ride id:{}", ride.getId(), e);
    }
    if (imageData == null || imageData.length == 0) {
      try {
        return s3StorageService.loadFile(noRideImageKey);
      } catch (IOException ex) {
        throw new EmailException(ex);
      }
    }
    return imageData;
  }

  private String getDriverPhoto(Ride ride) {
    Driver driver = ride.getActiveDriver().getDriver();
    return Optional.ofNullable(documentService.findAvatarDocument(driver, DocumentType.DRIVER_PHOTO))
      .map(Document::getDocumentUrl)
      .orElse(null);
  }
}
