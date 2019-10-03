package com.rideaustin.test.setup;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.fixtures.RiderFixture;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;

@Component
public class BaseDispatchedRideInQueueSetup implements SetupAction<BaseDispatchedRideInQueueSetup> {

  @Inject
  private ActiveDriverFixtureProvider provider;
  private ActiveDriver firstDriver;
  private ActiveDriver secondDriver;

  @Inject
  private RiderFixture riderFixture;
  private Rider rider;

  @Override
  @Transactional
  public BaseDispatchedRideInQueueSetup setUp() throws Exception {
    firstDriver = provider.create().getFixture();
    secondDriver = provider.create().getFixture();
    rider = riderFixture.getFixture();
    return this;
  }

  public ActiveDriver getFirstDriver() {
    return firstDriver;
  }

  public ActiveDriver getSecondDriver() {
    return secondDriver;
  }

  public Rider getRider() {
    return rider;
  }
}
