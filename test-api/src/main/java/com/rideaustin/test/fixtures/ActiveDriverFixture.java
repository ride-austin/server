package com.rideaustin.test.fixtures;

import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Driver;

public class ActiveDriverFixture extends AbstractFixture<ActiveDriver> {

  private DriverFixture driverFixture;
  private ActiveDriverStatus status;

  ActiveDriverFixture(DriverFixture driverFixture, ActiveDriverStatus status) {
    this.driverFixture = driverFixture;
    this.status = status;
  }

  public static ActiveDriverFixtureBuilder builder() {
    return new ActiveDriverFixtureBuilder();
  }

  @Override
  protected ActiveDriver createObject() {
    Driver driver = driverFixture.getFixture();
    return ActiveDriver.builder()
      .driver(driver)
      .selectedCar(driver.getCars().iterator().next())
      .status(status)
      .cityId(1L)
      .build();
  }

  public static class ActiveDriverFixtureBuilder {
    private DriverFixture driverFixture;
    private ActiveDriverStatus status;

    public ActiveDriverFixture.ActiveDriverFixtureBuilder driverFixture(DriverFixture driverFixture) {
      this.driverFixture = driverFixture;
      return this;
    }

    public ActiveDriverFixtureBuilder status(ActiveDriverStatus status) {
      this.status = status;
      return this;
    }

    public ActiveDriverFixture build() {
      return new ActiveDriverFixture(driverFixture, status);
    }

  }
}
