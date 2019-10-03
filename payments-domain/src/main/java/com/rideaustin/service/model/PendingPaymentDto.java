package com.rideaustin.service.model;

import java.util.Date;

import org.joda.money.Money;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryProjection;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.utils.SafeZeroUtils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@ApiModel
public class PendingPaymentDto {
  @ApiModelProperty(required = true)
  private final long rideId;
  @ApiModelProperty(required = true)
  private final Money amount;
  @ApiModelProperty
  private final Date willChargeOn;

  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public PendingPaymentDto(@JsonProperty("rideId") long rideId, @JsonProperty("amount") Money amount,
    @JsonProperty("willChargeOn") Date willChargeOn) {
    this.rideId = rideId;
    this.amount = amount;
    this.willChargeOn = willChargeOn;
  }

  @QueryProjection
  public PendingPaymentDto(Ride ride, Money amountCharged, Date willChargeOn) {
    this.rideId = ride.getId();
    this.amount = SafeZeroUtils.safeZero(amountCharged);
    this.willChargeOn = willChargeOn;
  }
}
