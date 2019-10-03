package com.rideaustin.rest.model;

import static com.rideaustin.utils.SafeZeroUtils.safeZero;

import java.util.Date;

import org.joda.money.Money;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.querydsl.core.annotations.QueryProjection;
import com.rideaustin.Constants;
import com.rideaustin.model.enums.CardBrand;
import com.rideaustin.model.enums.PaymentProvider;
import com.rideaustin.model.enums.PaymentStatus;
import com.rideaustin.model.enums.SplitFareStatus;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@ApiModel
@AllArgsConstructor
@JsonDeserialize(builder = FarePaymentDto.FarePaymentDtoBuilder.class)
public class FarePaymentDto {

  @ApiModelProperty(required = true)
  private final long id;
  @ApiModelProperty(required = true)
  private final long rideId;
  @ApiModelProperty(required = true)
  private final long riderId;
  @ApiModelProperty(required = true)
  private final String riderFullName;
  @ApiModelProperty
  private final String riderPhoto;
  @ApiModelProperty(required = true)
  private final SplitFareStatus status;
  @ApiModelProperty(required = true)
  private final String createdDate;
  @ApiModelProperty(required = true)
  private final String updatedDate;
  @ApiModelProperty(required = true)
  private final boolean mainRider;
  @ApiModelProperty(required = true)
  private final Money freeCreditCharged;
  @ApiModelProperty(required = true)
  private final Money stripeCreditCharge;
  @ApiModelProperty(required = true)
  private final RiderCardDto usedCard;
  @ApiModelProperty
  private final String chargeId;
  @ApiModelProperty(required = true)
  private final PaymentStatus paymentStatus;
  @ApiModelProperty(required = true)
  private final PaymentProvider paymentProvider;

  @QueryProjection
  public FarePaymentDto(Long id, Long rideId, Long riderId, String riderFullName, String riderPhoto, SplitFareStatus status,
    Date createdDate, Date updatedDate, boolean mainRider, Money freeCreditCharged, Money stripeCreditCharge,
    long usedCardId, String usedCardNumber, CardBrand usedCardBrand, boolean usedCardExpired, boolean usedCardPrimary,
    String chargeId, PaymentStatus paymentStatus, PaymentProvider paymentProvider) {
    this.id = id;
    this.rideId = rideId;
    this.riderId = riderId;
    this.riderFullName = riderFullName;
    this.riderPhoto = riderPhoto;
    this.status = status;
    this.createdDate = formatDate(createdDate);
    this.updatedDate = formatDate(updatedDate);
    this.mainRider = mainRider;
    this.freeCreditCharged = safeZero(freeCreditCharged);
    this.stripeCreditCharge = safeZero(stripeCreditCharge);
    this.usedCard = new RiderCardDto(usedCardId, usedCardNumber, usedCardBrand, usedCardExpired, usedCardPrimary, null, null);
    this.chargeId = chargeId;
    this.paymentProvider = paymentProvider;
    this.paymentStatus = paymentStatus;
  }

  private String formatDate(Date date) {
    return Constants.DATE_FORMATTER.format(date.toInstant().atZone(Constants.CST_ZONE));
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static class FarePaymentDtoBuilder {}

}
