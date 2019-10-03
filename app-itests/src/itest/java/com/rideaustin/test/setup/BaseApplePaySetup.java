package com.rideaustin.test.setup;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.fixtures.CarFixture;
import com.rideaustin.test.fixtures.DriverFixture;
import com.rideaustin.test.fixtures.RiderFixture;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;
import com.rideaustin.test.fixtures.providers.DriverFixtureProvider;

public abstract class BaseApplePaySetup<T> implements SetupAction<T> {

  @Inject
  protected ActiveDriverFixtureProvider activeDriverFixtureProvider;
  @Inject
  protected DriverFixtureProvider driverFixtureProvider;
  @Inject
  @Named("simpleRiderWithCharity")
  protected RiderFixture riderFixture;

  @Inject
  @Named("simpleDriver")
  protected DriverFixture driverFixture;

  @Inject
  @Qualifier("suvCar")
  protected CarFixture carFixture;

  protected Rider rider;
  protected ActiveDriver activeDriver;

  @Override
  @Transactional
  public T setUp() throws Exception {
    rider = riderFixture.getFixture();
    activeDriver = activeDriverFixtureProvider.create(driverFixtureProvider.create(carFixture)).getFixture();
    return getThis();
  }

  protected abstract T getThis();

  public Rider getRider() {
    return rider;
  }

  public ActiveDriver getActiveDriver() {
    return activeDriver;
  }
}
