package com.rideaustin.rest.model;

import java.util.Date;

import org.joda.money.Money;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@ApiModel
public class PaymentHistoryDto {

  @ApiModelProperty(required = true)
  private long farePaymentId;
  @ApiModelProperty(required = true)
  private long rideId;
  @ApiModelProperty(required = true)
  private String rideStartAddress;
  @ApiModelProperty
  private String rideEndAddress;
  @ApiModelProperty(required = true)
  private String driverFirstName;
  @ApiModelProperty(required = true)
  private String driverLastName;
  @ApiModelProperty
  private String driverNickName;
  @ApiModelProperty(required = true)
  private String driverPicture;
  @ApiModelProperty(required = true)
  private Long driverId;
  @ApiModelProperty(required = true)
  private boolean isMainRider;
  @ApiModelProperty(required = true)
  private Long mainRiderId;
  @ApiModelProperty(required = true)
  private String mainRiderFistName;
  @ApiModelProperty(required = true)
  private String mainRiderLastName;
  @ApiModelProperty(required = true)
  private String mainRiderPicture;
  @ApiModelProperty(required = true)
  private Money rideTotalFare;
  @ApiModelProperty(required = true)
  private Money freeCreditCharged;
  @ApiModelProperty(required = true)
  private Money stripeCreditCharge;
  @ApiModelProperty
  private Long usedCardId;
  @ApiModelProperty
  private String usedCardBrand;
  @ApiModelProperty
  private String cardNumber;
  @ApiModelProperty
  private String startedOn;
  @ApiModelProperty
  private String completedOn;
  @ApiModelProperty
  private Date completedOnUTC;
  @ApiModelProperty
  private String cancelledOn;
  @ApiModelProperty
  private Date cancelledOnUTC;
  @ApiModelProperty(required = true)
  private String rideStatus;
  @ApiModelProperty(required = true)
  private Double driverRating;
  @ApiModelProperty
  private String mapUrl;
  @ApiModelProperty(required = true)
  private String carBrand;
  @ApiModelProperty(required = true)
  private String carModel;
  @ApiModelProperty
  private String otherPaymentMethodUrl;
  @ApiModelProperty
  private Money campaignDiscount;
  @ApiModelProperty
  private String campaignDescription;
  @ApiModelProperty
  private String campaignProvider;
  @ApiModelProperty
  private String campaignDescriptionHistory;
}