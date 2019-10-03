package com.rideaustin.driverstatistic.rest;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@ApiModel
@AllArgsConstructor
public class DriverStatisticDto {

  @ApiModelProperty(required = true)
  private final String name;

  @ApiModelProperty(required = true)
  private final String description;

  @ApiModelProperty(required = true)
  private final int value;

  @ApiModelProperty(required = true)
  private final int outOfTotal;

}
