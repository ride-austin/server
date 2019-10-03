package com.rideaustin.test.setup;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;
import com.rideaustin.test.fixtures.providers.RiderFixtureProvider;

@Component
public class RA12788Setup implements SetupAction<RA12788Setup> {

  @Inject
  private RiderFixtureProvider riderFixtureProvider;
  @Inject
  private ActiveDriverFixtureProvider activeDriverFixtureProvider;
  private Rider mainRider;
  private Rider secondRider;
  private ActiveDriver driver;

  @Override
  @Transactional
  public RA12788Setup setUp() throws Exception {
    this.mainRider = riderFixtureProvider.create().getFixture();
    this.secondRider = riderFixtureProvider.create().getFixture();
    this.driver = activeDriverFixtureProvider.create().getFixture();
    return this;
  }

  public Rider getMainRider() {
    return mainRider;
  }

  public Rider getSecondRider() {
    return secondRider;
  }

  public ActiveDriver getDriver() {
    return driver;
  }
}
