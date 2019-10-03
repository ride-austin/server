package com.rideaustin.driverstatistic.model;

public class DriverStatisticNotFoundException extends Exception {

  DriverStatisticNotFoundException(DriverStatisticId driverStatisticId) {
    super(String.format("driver statistic not found by id '%s'", driverStatisticId.getId()));
  }

  public DriverStatisticNotFoundException(DriverId driverId) {
    super(String.format("driver statistic not found by driver id '%s'", driverId.getId()));

  }
}
