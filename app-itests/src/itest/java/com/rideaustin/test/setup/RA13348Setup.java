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
public class RA13348Setup implements SetupAction<RA13348Setup> {

  @Inject
  private RiderFixture riderFixture;
  private Rider rider;

  @Inject
  private ActiveDriverFixtureProvider activeDriverFixtureProvider;
  private ActiveDriver activeDriver;

  @Inject
  private DriverFixtureProvider driverFixtureProvider;

  @Inject
  @Named("hondaCar")
  private CarFixture carFixture;

  @Override
  @Transactional
  public RA13348Setup setUp() throws Exception {
    this.rider = riderFixture.getFixture();
    this.activeDriver = activeDriverFixtureProvider.create(driverFixtureProvider.create(carFixture, 0)).getFixture();
    return this;
  }

  public Rider getRider() {
    return rider;
  }

  public ActiveDriver getActiveDriver() {
    return activeDriver;
  }
}
