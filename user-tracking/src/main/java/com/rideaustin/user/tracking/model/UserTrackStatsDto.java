package com.rideaustin.user.tracking.model;

import java.math.BigDecimal;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import com.querydsl.core.annotations.QueryProjection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@Getter
@ApiModel
public class UserTrackStatsDto {

  @ApiModelProperty(required = true)
  private final String source;
  @ApiModelProperty(required = true)
  private final String campaign;
  @ApiModelProperty(required = true)
  private final String medium;
  @ApiModelProperty(required = true)
  private final long rides;
  @ApiModelProperty(required = true)
  private final Money fare;
  @ApiModelProperty(required = true)
  private final Money driverPayment;
  @ApiModelProperty(required = true)
  private final Money raPayment;
  @ApiModelProperty(required = true)
  private final double distance;

  @QueryProjection
  public UserTrackStatsDto(String source, String campaign, String medium, long rides, BigDecimal fare, BigDecimal driverPayment,
    BigDecimal raPayment, double distance) {
    this.source = source;
    this.campaign = campaign;
    this.medium = medium;
    this.rides = rides;
    this.fare = Money.of(CurrencyUnit.USD, fare);
    this.driverPayment = Money.of(CurrencyUnit.USD, driverPayment);
    this.raPayment = Money.of(CurrencyUnit.USD, raPayment);
    this.distance = distance;
  }

}
