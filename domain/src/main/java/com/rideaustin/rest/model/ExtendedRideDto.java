package com.rideaustin.rest.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryProjection;
import com.rideaustin.Constants;
import com.rideaustin.model.enums.RideStatus;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@ApiModel
public class ExtendedRideDto {

  @ApiModelProperty(required = true)
  private final Long rideId;
  @ApiModelProperty(required = true)
  private final Long riderId;
  @ApiModelProperty(required = true)
  private final String riderFirstName;
  @ApiModelProperty(required = true)
  private final String riderLastName;
  @ApiModelProperty(required = true)
  private final String carType;
  @Setter
  @ApiModelProperty(required = true)
  private Long driverId;
  @Setter
  @ApiModelProperty(required = true)
  private String driverFirstName;
  @Setter
  @ApiModelProperty(required = true)
  private String driverLastName;
  @Setter
  @ApiModelProperty
  private Date started;
  @ApiModelProperty
  private final Date completed;
  @ApiModelProperty
  private final BigDecimal distance;
  @ApiModelProperty(required = true)
  private final RideStatus status;
  @Setter
  @ApiModelProperty(required = true)
  private String driverAppVersion;
  @ApiModelProperty(required = true)
  private final String riderAppVersion;
  @ApiModelProperty(required = true)
  private final AddressDto startAddress;
  @ApiModelProperty
  private final AddressDto endAddress;
  @Setter
  @ApiModelProperty(required = true)
  private String driverPhoneNumber;
  @Setter
  @ApiModelProperty
  private Double driverLatitude;
  @Setter
  @ApiModelProperty
  private Double driverLongitude;
  @Setter
  @ApiModelProperty
  private Long activeDriverId;

  @QueryProjection
  public ExtendedRideDto(Long rideId, Long riderId, String riderFirstName, String riderLastName, String carType,
    Date started, Date completed, BigDecimal distance, RideStatus status, String riderAppVersion,
    String startZipcode, String startAddress, String endZipcode, String endAddress, Long activeDriverId) {
    this.rideId = rideId;
    this.riderId = riderId;
    this.riderFirstName = riderFirstName;
    this.riderLastName = riderLastName;
    this.carType = carType;
    this.started = started;
    this.completed = completed;
    this.distance = distance;
    this.status = status;
    this.riderAppVersion = riderAppVersion;
    this.startAddress = new AddressDto(startAddress, startZipcode);
    this.endAddress = new AddressDto(endAddress, endZipcode);
    this.activeDriverId = activeDriverId;
  }

  @JsonProperty
  @ApiModelProperty
  public BigDecimal getDistanceInMiles() {
    return distance == null ? null :
      Constants.MILES_PER_METER.multiply(distance).setScale(2, RoundingMode.HALF_UP);
  }

  @Getter
  @ApiModel
  @RequiredArgsConstructor
  private static class AddressDto {

    @ApiModelProperty(required = true)
    private final String address;
    @ApiModelProperty(required = true)
    private final String zipCode;

  }
}
