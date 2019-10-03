package com.rideaustin.rest.model;

import java.util.List;

import com.rideaustin.service.model.PendingPaymentDto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@ApiModel
public class CurrentRiderDto {

  @ApiModelProperty(required = true)
  private final RiderDto rider;
  @ApiModelProperty(required = true)
  private final MobileRiderRideDto ride;
  @ApiModelProperty(required = true)
  private final List<RiderCardDto> cards;
  @ApiModelProperty(required = true)
  private final List<PendingPaymentDto> unpaid;

}
