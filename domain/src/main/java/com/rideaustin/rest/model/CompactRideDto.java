package com.rideaustin.rest.model;

import java.math.BigDecimal;
import java.util.Date;

import org.joda.money.Money;

import com.querydsl.core.annotations.QueryProjection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@ApiModel
public class CompactRideDto {

  @ApiModelProperty(required = true)
  private final Long id;
  @ApiModelProperty(required = true)
  private final Long riderId;
  @ApiModelProperty(required = true)
  private final String riderFullname;
  @ApiModelProperty(required = true)
  private final String carCategory;
  @ApiModelProperty(required = true)
  private final Long driverId;
  @ApiModelProperty(required = true)
  private final String driverFullname;
  @ApiModelProperty
  private final Date startedOn;
  @ApiModelProperty
  private final Date completedOn;
  @Setter
  @ApiModelProperty
  private BigDecimal distanceTravelled;
  @ApiModelProperty
  private final Money tip;
  @ApiModelProperty
  private final Date tippedOn;
  @ApiModelProperty
  private final Date cancelledOn;

  @QueryProjection
  public CompactRideDto(Long id, Long riderId, String riderFirstName, String riderLastName, String carCategory, Long driverId,
    String driverFirstName, String driverLastName, Date startedOn, Date completedOn, BigDecimal distanceTravelled, Money tip,
    Date tippedOn, Date cancelledOn) {
    this.id = id;
    this.riderId = riderId;
    this.riderFullname = String.format("%s %s", riderFirstName, riderLastName);
    this.carCategory = carCategory;
    this.driverId = driverId;
    this.driverFullname = String.format("%s %s", driverFirstName, driverLastName);
    this.startedOn = startedOn;
    this.completedOn = completedOn;
    this.distanceTravelled = distanceTravelled;
    this.tip = tip;
    this.tippedOn = tippedOn;
    this.cancelledOn = cancelledOn;
  }

}
