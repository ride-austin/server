package com.rideaustin.model.rest;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@ApiModel
@RequiredArgsConstructor
public class CampaignSubscriptionDto {
  @ApiModelProperty(required = true)
  private final long id;
  @ApiModelProperty(required = true)
  private final String name;
  @ApiModelProperty(required = true)
  private final boolean subscribed;
}
