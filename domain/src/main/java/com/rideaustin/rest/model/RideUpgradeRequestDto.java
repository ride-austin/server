package com.rideaustin.rest.model;

import java.math.BigDecimal;

import com.rideaustin.model.enums.RideUpgradeRequestStatus;
import com.rideaustin.model.ride.RideUpgradeRequest;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@ApiModel
@NoArgsConstructor
public class RideUpgradeRequestDto {
  @ApiModelProperty(required = true)
  private String target;
  @ApiModelProperty(required = true)
  private String source;
  @ApiModelProperty(required = true)
  private RideUpgradeRequestStatus status;
  @ApiModelProperty(required = true)
  private BigDecimal surgeFactor;

  public RideUpgradeRequestDto(RideUpgradeRequest request) {
    this.source = request.getSource();
    this.target = request.getTarget();
    this.status = request.getStatus();
    this.surgeFactor = request.getSurgeFactor();
  }
}
