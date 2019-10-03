package com.rideaustin.rest.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@ApiModel
@AllArgsConstructor
public class DriverEmailHistoryItemDto {
  @ApiModelProperty
  private final long id;
  @ApiModelProperty
  private final String date;
  @ApiModelProperty
  private final String actor;
  @ApiModelProperty
  private final String communicationType;
  @ApiModelProperty
  private final long communicationTypeId;

}
