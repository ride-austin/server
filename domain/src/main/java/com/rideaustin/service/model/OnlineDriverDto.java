package com.rideaustin.service.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryProjection;
import com.rideaustin.Constants;
import com.rideaustin.model.DistanceAware;
import com.rideaustin.model.DrivingTimeAware;
import com.rideaustin.model.LocationAware;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.service.location.model.LocationObject;

import lombok.Getter;
import lombok.Setter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OnlineDriverDto implements DistanceAware, LocationAware, DrivingTimeAware {

  private final long id;
  @Setter
  private ActiveDriverStatus status;
  @Setter
  private LocationObject locationObject;
  @Setter
  private Integer availableDriverTypesBitmask;
  @Setter
  private Integer availableCarCategoriesBitmask;
  private final Long cityId;
  private final long driverId;
  private final Long userId;
  private final String fullName;
  private final String phoneNumber;
  private final String licenseNumber;

  @JsonIgnore
  private Double directDistanceToRider;
  @JsonIgnore
  private Long drivingDistanceToRider;
  @JsonIgnore
  private Long drivingTimeToRider;
  @Setter
  private boolean eligibleForStacking;
  @Setter
  private ConsecutiveDeclinedRequestsData consecutiveDeclinedRequests;

  /**
   * use this constuctor in tests only
   */
  public OnlineDriverDto() {
    this.id = 1L;
    this.cityId = 1L;
    this.driverId = 1L;
    this.userId = 1L;
    this.fullName = "";
    this.phoneNumber = "";
    this.licenseNumber = "";
  }

  public OnlineDriverDto(ActiveDriverInfo activeDriver) {
    this(activeDriver.getId(), activeDriver.getStatus(), activeDriver.getDriver().getId(),
      activeDriver.getDriver().getUser().getId(), activeDriver.getAvailableCarCategoriesBitmask(),
      activeDriver.getAvailableDriverTypesBitmask(), activeDriver.getCityId(), activeDriver.getDriver().getFullName(),
      activeDriver.getDriver().getPhoneNumber(), new ConsecutiveDeclinedRequestsData(activeDriver.getCarCategories()),
      activeDriver.getCar().getLicense());
  }

  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public OnlineDriverDto(@JsonProperty("id") long id, @JsonProperty("status") ActiveDriverStatus status,
    @JsonProperty("driverId") Long driverId, @JsonProperty("userId") Long userId,
    @JsonProperty("availableCarCategoriesBitmask") Integer availableCarCategoriesBitmask,
    @JsonProperty("availableDriverTypesBitmask") Integer availableDriverTypesBitmask, @JsonProperty("cityId") long cityId,
    @JsonProperty("fullName") String fullName, @JsonProperty("phoneNumber") String phoneNumber,
    @JsonProperty("consecutiveDeclinedRequests") ConsecutiveDeclinedRequestsData consecutiveDeclinedRequests,
    @JsonProperty("licenseNumber") String licenseNumber) {
    this.id = id;
    this.status = status;
    this.driverId = driverId;
    this.userId = userId;
    this.availableCarCategoriesBitmask = availableCarCategoriesBitmask;
    this.availableDriverTypesBitmask = availableDriverTypesBitmask;
    this.cityId = cityId;
    this.fullName = fullName;
    this.phoneNumber = phoneNumber;
    this.consecutiveDeclinedRequests = consecutiveDeclinedRequests;
    this.licenseNumber = licenseNumber;
  }

  @QueryProjection
  public OnlineDriverDto(long id, ActiveDriverStatus status, Long driverId, Long userId,
    String fullName, String phoneNumber, String licenseNumber) {
    this.id = id;
    this.status = status;
    this.driverId = driverId;
    this.userId = userId;
    this.fullName = fullName;
    this.phoneNumber = phoneNumber;
    this.cityId = Constants.DEFAULT_CITY_ID;
    this.licenseNumber = licenseNumber;
  }

  public long getId() {
    return id;
  }

  @Override
  @JsonIgnore
  public double getLatitude() {
    return locationObject.getLatitude();
  }

  @Override
  @JsonIgnore
  public double getLongitude() {
    return locationObject.getLongitude();
  }

  @Override
  public Long getDrivingTimeToRider() {
    return drivingTimeToRider;
  }

  @Override
  public void setDrivingTimeToRider(Long drivingTimeToRider) {
    this.drivingTimeToRider = drivingTimeToRider;
  }

  @Override
  public LocationObject getLocationObject() {
    return locationObject;
  }

  public Integer getAvailableDriverTypesBitmask() {
    return availableDriverTypesBitmask;
  }

  public Integer getAvailableCarCategoriesBitmask() {
    return availableCarCategoriesBitmask;
  }

  @Override
  public Double getDirectDistanceToRider() {
    return directDistanceToRider;
  }

  @Override
  public void setDirectDistanceToRider(Double directDistanceToRider) {
    this.directDistanceToRider = directDistanceToRider;
  }

  @Override
  public Long getDrivingDistanceToRider() {
    return drivingDistanceToRider;
  }

  @Override
  public void setDrivingDistanceToRider(Long drivingDistanceToRider) {
    this.drivingDistanceToRider = drivingDistanceToRider;
  }

  @JsonIgnore
  public Date getLocationUpdatedOn() {
    return locationObject.getLocationUpdateDate();
  }

  public long getDriverId() {
    return driverId;
  }
}
