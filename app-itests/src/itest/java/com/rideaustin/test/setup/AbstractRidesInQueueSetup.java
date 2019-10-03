package com.rideaustin.test.setup;

import javax.inject.Inject;

import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.fixtures.RiderFixture;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;

public abstract class AbstractRidesInQueueSetup<T extends AbstractRidesInQueueSetup> implements SetupAction<T> {

  @Inject
  private ActiveDriverFixtureProvider activeDriverFixtureProvider;

  @Inject
  private RiderFixture riderFixture;

  private Rider rider;
  private ActiveDriver availableActiveDriver;

  @Override
  @Transactional
  public T setUp() throws Exception {
    this.rider = riderFixture.getFixture();
    this.availableActiveDriver = activeDriverFixtureProvider.create().getFixture();
    return doSetup();
  }

  protected abstract T doSetup();

  public Rider getRider() {
    return rider;
  }

  public ActiveDriver getAvailableActiveDriver() {
    return availableActiveDriver;
  }
}
