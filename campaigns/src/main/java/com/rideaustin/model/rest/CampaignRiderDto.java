package com.rideaustin.model.rest;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@ApiModel
@RequiredArgsConstructor
public class CampaignRiderDto {
  @ApiModelProperty(required = true)
  private final String firstName;
  @ApiModelProperty(required = true)
  private final String lastName;
  @ApiModelProperty(required = true)
  private final String email;

}
