package com.rideaustin.test.setup;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.fixtures.CarFixture;
import com.rideaustin.test.fixtures.RiderFixture;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;
import com.rideaustin.test.fixtures.providers.DriverFixtureProvider;

public abstract class BaseRedispatchTestSetup<T extends SetupAction<T>> implements SetupAction<T> {

  protected Rider rider;
  protected ActiveDriver firstActiveDriver;
  protected ActiveDriver secondActiveDriver;

  @Inject
  @Qualifier("suvCar")
  private CarFixture carFixture;

  @Inject
  @Named("simpleRiderWithCharity")
  protected RiderFixture riderFixture;

  @Inject
  protected ActiveDriverFixtureProvider activeDriverFixtureProvider;

  @Inject
  protected DriverFixtureProvider driverFixtureProvider;

  @Override
  @Transactional
  public T setUp() throws Exception {
    rider = riderFixture.getFixture();

    firstActiveDriver = createDriver();
    secondActiveDriver = createDriver();
    return getThis();
  }

  protected ActiveDriver createDriver() {
    return activeDriverFixtureProvider.create(driverFixtureProvider.create(carFixture)).getFixture();
  }

  protected abstract T getThis();

  public Rider getRider() {
    return rider;
  }

  public ActiveDriver getFirstActiveDriver() {
    return firstActiveDriver;
  }

  public ActiveDriver getSecondActiveDriver() {
    return secondActiveDriver;
  }
}
