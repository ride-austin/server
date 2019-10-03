package com.rideaustin.assemblers;

import java.util.Date;
import java.util.Optional;

import javax.inject.Inject;

import org.joda.money.Money;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.rideaustin.Constants;
import com.rideaustin.model.Address;
import com.rideaustin.model.Campaign;
import com.rideaustin.model.CampaignProvider;
import com.rideaustin.model.Document;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.enums.PaymentProvider;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.splitfare.FarePayment;
import com.rideaustin.model.user.Driver;
import com.rideaustin.rest.model.PaymentHistoryDto;
import com.rideaustin.service.CampaignService;
import com.rideaustin.service.DocumentService;
import com.rideaustin.service.thirdparty.S3StorageService;
import com.rideaustin.utils.DateUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PaymentHistoryDtoAssembler implements SingleSideAssembler<FarePayment, PaymentHistoryDto>, Converter<FarePayment, PaymentHistoryDto> {

  private final DocumentService documentService;
  private final S3StorageService s3StorageService;
  private final CampaignService campaignService;

  @Override
  public PaymentHistoryDto toDto(FarePayment sp) {
    PaymentHistoryDto.PaymentHistoryDtoBuilder builder = PaymentHistoryDto.builder()
      .farePaymentId(sp.getId())
      .rideId(sp.getRide().getId())
      .cancelledOn(getDate(sp.getRide().getCancelledOn()))
      .cancelledOnUTC(sp.getRide().getCancelledOn())
      .completedOn(getDate(sp.getRide().getCompletedOn()))
      .completedOnUTC(sp.getRide().getCompletedOn())
      .startedOn(getDate(sp.getRide().getStartedOn()))
      .driverRating(Optional.ofNullable(sp.getRide().getDriverRating()).orElse(null));
    if (sp.getUsedCard() != null) {
      builder.cardNumber(sp.getUsedCard().getCardNumber())
        .usedCardBrand(sp.getUsedCard().getCardBrand().name())
        .usedCardId(sp.getUsedCard().getId());
    }
    if (sp.getRide().getActiveDriver() != null) {
      Driver driver = sp.getRide().getActiveDriver().getDriver();
      builder.driverNickName(driver.getUser().getNickName())
        .driverFirstName(driver.getFirstname())
        .driverLastName(driver.getLastname());
      String photoUrl = Optional.ofNullable(documentService.findAvatarDocument(driver, DocumentType.DRIVER_PHOTO)).map(Document::getDocumentUrl).orElse("");
      builder.driverPicture(photoUrl);
      String mapUrl = null;
      if (sp.getRide().getRideMapMinimized() != null) {
        mapUrl = s3StorageService.getSignedURL(sp.getRide().getRideMapMinimized());
      } else if (sp.getRide().getRideMap() != null) {
        mapUrl = s3StorageService.getSignedURL(sp.getRide().getRideMap());
      }
      builder.mapUrl(mapUrl);
      if (sp.getRide().getActiveDriver().getSelectedCar() != null) {
        builder.carBrand(sp.getRide().getActiveDriver().getSelectedCar().getMake())
          .carModel(sp.getRide().getActiveDriver().getSelectedCar().getModel());
      }
    }
    if (sp.getRide().getStatus() == RideStatus.COMPLETED) {
      final Optional<Campaign> campaign = campaignService.findExistingCampaignForRide(sp.getRide());
      if (campaign.isPresent()) {
        final Money discount = sp.getRide().getTotalCharge().minus(campaign.get().adjustTotalCharge(sp.getRide().getTotalCharge()));
        builder.campaignDiscount(discount)
          .campaignDescription(campaign.map(Campaign::getDescription).orElse(""))
          .campaignProvider(campaign.map(Campaign::getProvider).map(CampaignProvider::getName).orElse(""))
          .campaignDescriptionHistory(campaign.map(Campaign::getTripHistoryDescription).orElse(""));
      }
    }
    return builder.freeCreditCharged(sp.getFreeCreditCharged())
      .rideStatus(sp.getRide().getStatus().toString())
      .isMainRider(sp.isMainRider())
      .mainRiderFistName(sp.getRide().getRider().getFirstname())
      .mainRiderLastName(sp.getRide().getRider().getLastname())
      .mainRiderId(sp.getRide().getRider().getId())
      .mainRiderPicture(sp.getRide().getRider().getUser().getPhotoUrl())
      .rideEndAddress(getAddress(sp.getRide().getEnd()))
      .rideStartAddress(getAddress(sp.getRide().getStart()))
      .rideTotalFare(sp.getRide().getTotalFare())
      .stripeCreditCharge(sp.getStripeCreditCharge())
      .otherPaymentMethodUrl(Optional.ofNullable(sp.getProvider()).map(PaymentProvider::getIcon).orElse(null))
      .build();
  }

  private String getAddress(Address address) {
    return Optional.ofNullable(address).map(Address::getAddress).orElse(null);
  }

  private String getDate(Date date) {
    return Optional.ofNullable(date).map(d -> Constants.DATETIME_FORMATTER.format(DateUtils.dateToInstant(d))).orElse(null);
  }

  @Override
  public PaymentHistoryDto convert(FarePayment source) {
    return toDto(source);
  }
}
