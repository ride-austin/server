package com.rideaustin.service.model;

import com.querydsl.core.annotations.QueryProjection;
import com.rideaustin.model.enums.RideStatus;

public class AssociatedRide {
  public final Long id;
  public final RideStatus rideStatus;
  public final String riderPhone;
  public final String driverPhone;

  @QueryProjection
  public AssociatedRide(Long id, RideStatus rideStatus, String riderPhone, String driverPhone) {
    this.id = id;
    this.rideStatus = rideStatus;
    this.riderPhone = riderPhone;
    this.driverPhone = driverPhone;
  }

  public RideStatus getRideStatus() {
    return rideStatus;
  }
}
