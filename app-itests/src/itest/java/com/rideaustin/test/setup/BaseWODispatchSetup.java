package com.rideaustin.test.setup;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.fixtures.CarFixture;
import com.rideaustin.test.fixtures.RiderFixture;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;
import com.rideaustin.test.fixtures.providers.DriverFixtureProvider;

public abstract class BaseWODispatchSetup<T> implements SetupAction<T> {

  @Inject
  protected ActiveDriverFixtureProvider activeDriverFixtureProvider;

  @Inject
  protected DriverFixtureProvider driverFixtureProvider;

  @Inject
  protected RiderFixture riderFixture;

  @Inject
  @Named("premiumCar")
  protected CarFixture carFixture;

  protected Rider rider;
  protected ActiveDriver woDriver;
  protected ActiveDriver regularDriver;
  protected ActiveDriver woFPDriver;

  @Override
  @Transactional
  public T setUp() throws Exception {
    rider = riderFixture.getFixture();
    regularDriver = activeDriverFixtureProvider.create(driverFixtureProvider.create(carFixture, 4)).getFixture();
    woDriver = activeDriverFixtureProvider.create(driverFixtureProvider.create(carFixture, 1)).getFixture();
    woFPDriver = activeDriverFixtureProvider.create(driverFixtureProvider.create(carFixture, 5)).getFixture();
    return getThis();
  }

  protected abstract T getThis();

  public Rider getRider() {
    return rider;
  }

  public ActiveDriver getWoDriver() {
    return woDriver;
  }

  public ActiveDriver getRegularDriver() {
    return regularDriver;
  }

  public ActiveDriver getWoFPDriver() {
    return woFPDriver;
  }
}
