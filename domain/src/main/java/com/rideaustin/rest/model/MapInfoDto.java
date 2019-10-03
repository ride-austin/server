package com.rideaustin.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.annotations.QueryProjection;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.enums.RideStatus;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@ApiModel
public class MapInfoDto {

  @ApiModelProperty(required = true)
  private final long id;
  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private final Long activeDriverId;
  @ApiModelProperty(required = true)
  private final Double startLocationLat;
  @ApiModelProperty(required = true)
  private final Double startLocationLong;
  @ApiModelProperty(required = true)
  private final RideStatus status;
  @ApiModelProperty(required = true)
  private final RiderInfo rider;
  @Setter
  @ApiModelProperty
  private ActiveDriverInfo activeDriver;

  public MapInfoDto() {
    id = 0L;
    activeDriverId = null;
    startLocationLat = null;
    startLocationLong = null;
    status = null;
    rider = null;
  }

  @QueryProjection
  public MapInfoDto(long id, Long activeDriverId, Double startLocationLat, Double startLocationLong, RideStatus status,
    long riderId) {
    this.id = id;
    this.activeDriverId = activeDriverId;
    this.startLocationLat = startLocationLat;
    this.startLocationLong = startLocationLong;
    this.status = status;
    this.rider = new RiderInfo(riderId);
  }

  @Getter
  @ApiModel
  @RequiredArgsConstructor
  private static class RiderInfo {
    @ApiModelProperty(required = true)
    final long id;
  }

  @Getter
  @ApiModel
  @RequiredArgsConstructor
  public static class ActiveDriverInfo {
    @ApiModelProperty(required = true)
    final double latitude;
    @ApiModelProperty(required = true)
    final double longitude;
    @ApiModelProperty(required = true)
    final DriverInfo driver;
    @ApiModelProperty(required = true)
    final ActiveDriverStatus status;
  }

  @Getter
  @ApiModel
  @RequiredArgsConstructor
  public static class DriverInfo {
    @ApiModelProperty(required = true)
    final long id;
    @ApiModelProperty(required = true)
    final String fullName;
    @ApiModelProperty(required = true)
    final String phoneNumber;
  }

}
