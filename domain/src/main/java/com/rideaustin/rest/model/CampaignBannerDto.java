package com.rideaustin.rest.model;

import org.joda.money.Money;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@ApiModel
@RequiredArgsConstructor
public class CampaignBannerDto {

  @ApiModelProperty(notes = "Unique ID of a ride campaign")
  private final long id;
  @ApiModelProperty(notes = "Text to be shown in rider app")
  private final String bannerText;
  @ApiModelProperty(notes = "Icon to be shown in rider app")
  private final String bannerIcon;
  @ApiModelProperty(notes = "Flag to indicate whether campaign area map should be shown in rider app")
  private final boolean shouldShowMap;
  @ApiModelProperty(notes = "Flag to indicate whether campaign details should be shown in rider app")
  private final boolean shouldShowDetail;
  @Setter
  @ApiModelProperty(notes = "Total estimated fare after campaign discounts have been applied")
  private Money estimatedFare;

}
