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
public class RA11352Setup implements SetupAction<RA11352Setup> {
  @Inject
  private ActiveDriverFixtureProvider activeDriverFixtureProvider;
  @Inject
  private DriverFixtureProvider driverFixtureProvider;
  @Inject
  private RiderFixture riderFixture;
  @Inject
  @Named("suvCar")
  protected CarFixture carFixture;

  private ActiveDriver activeDriver;
  private Rider rider;

  @Override
  @Transactional
  public RA11352Setup setUp() throws Exception {
    this.rider = riderFixture.getFixture();
    this.activeDriver = activeDriverFixtureProvider.create(driverFixtureProvider.create(carFixture)).getFixture();
    return this;
  }

  public ActiveDriver getActiveDriver() {
    return activeDriver;
  }

  public Rider getRider() {
    return rider;
  }
}
