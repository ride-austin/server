package com.rideaustin.test.setup;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.fixtures.CarFixture;
import com.rideaustin.test.fixtures.RiderFixture;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;
import com.rideaustin.test.fixtures.providers.DriverFixtureProvider;

@Component
public class RA137WODCDriverSetup implements SetupAction<RA137WODCDriverSetup> {

  @Inject
  @Named("allTypesCar")
  private CarFixture carFixture;
  @Inject
  private DriverFixtureProvider driverFixtureProvider;
  @Inject
  private ActiveDriverFixtureProvider activeDriverFixtureProvider;
  private ActiveDriver driver;

  @Inject
  private RiderFixture riderFixture;
  private Rider rider;

  @Override
  @Transactional
  public RA137WODCDriverSetup setUp() throws Exception {
    this.rider = riderFixture.getFixture();
    this.driver = activeDriverFixtureProvider.create(driverFixtureProvider.create(carFixture, 3)).getFixture();
    return this;
  }

  public ActiveDriver getDriver() {
    return driver;
  }

  public Rider getRider() {
    return rider;
  }
}
