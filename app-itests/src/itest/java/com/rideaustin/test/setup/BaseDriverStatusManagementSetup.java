package com.rideaustin.test.setup;

import javax.inject.Inject;

import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;
import com.rideaustin.test.fixtures.providers.DriverFixtureProvider;
import com.rideaustin.test.fixtures.providers.RiderFixtureProvider;

public abstract class BaseDriverStatusManagementSetup<T> implements SetupAction<T> {

  @Inject
  protected RiderFixtureProvider riderFixtureProvider;

  @Inject
  protected DriverFixtureProvider driverFixtureProvider;

  @Inject
  protected ActiveDriverFixtureProvider activeDriverFixtureProvider;

  private Rider rider;
  protected ActiveDriver activeDriver;

  @Override
  @Transactional
  public T setUp() throws Exception {
    this.rider = riderFixtureProvider.create().getFixture();
    return doSetup();
  }

  protected abstract T doSetup();

  public Rider getRider() {
    return rider;
  }

  public ActiveDriver getActiveDriver() {
    return activeDriver;
  }
}
