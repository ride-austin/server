package com.rideaustin.test.setup;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;
import com.rideaustin.test.fixtures.providers.RiderFixtureProvider;

@Component
public class UpfrontPricingSetup implements SetupAction<UpfrontPricingSetup> {

  @Inject
  private ActiveDriverFixtureProvider provider;
  @Inject
  private RiderFixtureProvider riderFixtureProvider;

  private Rider rider;
  private ActiveDriver activeDriver;

  @Override
  @Transactional
  public UpfrontPricingSetup setUp() throws Exception {
    rider = riderFixtureProvider.create().getFixture();
    activeDriver = provider.create().getFixture();
    return this;
  }

  public Rider getRider() {
    return rider;
  }

  public ActiveDriver getActiveDriver() {
    return activeDriver;
  }
}
