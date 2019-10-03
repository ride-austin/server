package com.rideaustin.rest.model;

import java.util.Date;

import org.joda.money.Money;

import com.querydsl.core.annotations.QueryProjection;
import com.rideaustin.model.enums.RideStatus;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@ApiModel
public class RideHistoryDto {
  @ApiModelProperty(required = true)
  private final long id;
  @ApiModelProperty(required = true)
  private final RideStatus status;
  @ApiModelProperty(required = true)
  private final String startAddress;
  @ApiModelProperty(required = true)
  private final String endAddress;
  @ApiModelProperty(required = true)
  private final String driver;
  @ApiModelProperty(required = true)
  private final String rider;
  @Setter
  @ApiModelProperty
  private Date startedOn;
  @ApiModelProperty
  private final Date completedOn;
  @ApiModelProperty
  private final Date cancelledOn;
  @ApiModelProperty
  private final Money estimatedFare;
  @ApiModelProperty
  private final Money totalFare;
  @ApiModelProperty
  private final Money tip;
  @ApiModelProperty
  private final Date tippedOn;

  @QueryProjection
  public RideHistoryDto(long id, RideStatus status, String startAddress, String endAddress, String driver, String rider,
    Date startedOn, Date completedOn, Date cancelledOn, Money estimatedFare, Money totalFare, Money tip, Date tippedOn) {
    this.id = id;
    this.status = status;
    this.startAddress = startAddress;
    this.endAddress = endAddress;
    this.driver = driver;
    this.rider = rider;
    this.startedOn = startedOn;
    this.completedOn = completedOn;
    this.cancelledOn = cancelledOn;
    this.estimatedFare = estimatedFare;
    this.totalFare = totalFare;
    this.tip = tip;
    this.tippedOn = tippedOn;
  }
}
