package com.rideaustin.model;

public interface DistanceAware {

  Double getDirectDistanceToRider();
  void setDirectDistanceToRider(Double directDistanceToRider);

  Long getDrivingDistanceToRider();
  void setDrivingDistanceToRider(Long drivingDistanceToRider);
}
