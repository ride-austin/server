package com.rideaustin.test.setup;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;
import com.rideaustin.test.fixtures.providers.RiderFixtureProvider;

@Component
public class RA14466Setup implements SetupAction<RA14466Setup> {

  @Inject
  private ActiveDriverFixtureProvider activeDriverFixtureProvider;
  @Inject
  private RiderFixtureProvider riderFixtureProvider;

  private Rider firstRider;
  private Rider secondRider;
  private ActiveDriver driver;

  @Override
  @Transactional
  public RA14466Setup setUp() throws Exception {
    firstRider = riderFixtureProvider.create().getFixture();
    secondRider = riderFixtureProvider.create().getFixture();
    driver = activeDriverFixtureProvider.create().getFixture();
    return this;
  }

  public Rider getFirstRider() {
    return firstRider;
  }

  public Rider getSecondRider() {
    return secondRider;
  }

  public ActiveDriver getDriver() {
    return driver;
  }
}
