package com.rideaustin.test.setup;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.fixtures.RiderFixture;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;

@Component
public class RA99LimitDestinationUpdatesSetup implements SetupAction<RA99LimitDestinationUpdatesSetup> {

  @Inject
  private ActiveDriverFixtureProvider activeDriverFixtureProvider;
  private ActiveDriver activeDriver;

  @Inject
  private RiderFixture riderFixture;
  private Rider rider;

  @Override
  @Transactional
  public RA99LimitDestinationUpdatesSetup setUp() throws Exception {
    activeDriver = activeDriverFixtureProvider.create().getFixture();
    rider = riderFixture.getFixture();
    return this;
  }

  public ActiveDriver getActiveDriver() {
    return activeDriver;
  }

  public Rider getRider() {
    return rider;
  }
}
