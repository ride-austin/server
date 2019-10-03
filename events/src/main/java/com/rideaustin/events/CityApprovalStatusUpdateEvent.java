package com.rideaustin.events;

import com.rideaustin.model.enums.CityApprovalStatus;
import com.rideaustin.model.user.Driver;

public class CityApprovalStatusUpdateEvent {

  private final CityApprovalStatus status;
  private final Driver driver;

  public CityApprovalStatusUpdateEvent(CityApprovalStatus status, Driver driver) {
    this.status = status;
    this.driver = driver;
  }

  public CityApprovalStatus getStatus() {
    return status;
  }

  public Driver getDriver() {
    return driver;
  }

}
