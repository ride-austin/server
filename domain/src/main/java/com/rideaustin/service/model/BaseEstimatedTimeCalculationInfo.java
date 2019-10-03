package com.rideaustin.service.model;

import com.rideaustin.model.DistanceAware;
import com.rideaustin.model.DrivingTimeAware;
import com.rideaustin.model.LocationAware;
import com.rideaustin.service.location.model.LocationObject;

import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class BaseEstimatedTimeCalculationInfo implements LocationAware, DistanceAware, DrivingTimeAware {
  private final long activeDriverId;
  @Setter
  private Long drivingTimeToRider;
  @Setter
  private Long drivingDistanceToRider;
  @Setter
  private LocationObject locationObject;

  public BaseEstimatedTimeCalculationInfo(long activeDriverId) {
    this.activeDriverId = activeDriverId;
  }

  @Override
  public long getId() {
    return activeDriverId;
  }

  @Override
  public double getLatitude() {
    return locationObject.getLatitude();
  }

  @Override
  public double getLongitude() {
    return locationObject.getLongitude();
  }

  @Override
  public Double getDirectDistanceToRider() {
    return null;
  }

  @Override
  public void setDirectDistanceToRider(Double directDistanceToRider) {
    //do nothing
  }
}
