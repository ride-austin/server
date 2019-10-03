package com.rideaustin.rest.model;

import java.util.List;
import java.util.Set;

import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.Car;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class RideTrackingShareDto {

  @ApiModelProperty(required = true)
  private Long rideId;
  @ApiModelProperty(required = true)
  private Long cityId;
  @ApiModelProperty(required = true)
  private Long riderId;
  @ApiModelProperty(required = true)
  private String riderFirstName;
  @ApiModelProperty(required = true)
  private String riderLastName;
  @ApiModelProperty(required = true)
  private String riderPhoto;
  @ApiModelProperty(required = true)
  private RideStatus status;
  @ApiModelProperty(required = true)
  private Long driverId;
  @ApiModelProperty(required = true)
  private String driverFirstName;
  @ApiModelProperty(required = true)
  private String driverLastName;
  @ApiModelProperty(required = true)
  private String driverPhoto;
  @ApiModelProperty(required = true)
  private String startAddress;
  @ApiModelProperty(required = true)
  private Location startLocation;
  @ApiModelProperty(required = true)
  private Location endLocation;
  @ApiModelProperty(required = true)
  private String endAddress;
  @ApiModelProperty(required = true)
  private Double currentSpeed;
  @ApiModelProperty(required = true)
  private Double currentHeading;
  @ApiModelProperty(required = true)
  private Double currentCourse;
  @ApiModelProperty(required = true)
  private String createdDate;
  @ApiModelProperty(required = true)
  private String updatedDate;
  @ApiModelProperty(required = true)
  private String startedOn;
  @ApiModelProperty(required = true)
  private String completedOn;
  @ApiModelProperty(required = true)
  private List<Location> locations;
  @ApiModelProperty(required = true)
  private Double driverRating;
  @ApiModelProperty(required = true)
  private Car driverCar;
  @ApiModelProperty(required = true)
  private Set<String> driverCarTypes;
  @ApiModelProperty(required = true)
  private String rideCarCategory;
  @ApiModelProperty(required = true)
  private String driverLicensePlate;
  @ApiModelProperty(required = true)
  private HeadingDirection currentHeadingDirection;
  @ApiModelProperty(required = true)
  private Location driverLocation;

}
