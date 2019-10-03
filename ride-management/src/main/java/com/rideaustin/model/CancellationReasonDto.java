package com.rideaustin.model;

import com.rideaustin.model.enums.CancellationReason;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@ApiModel
@AllArgsConstructor
public class CancellationReasonDto {
  @ApiModelProperty
  private final CancellationReason code;
  @ApiModelProperty
  private final String description;
}
