package com.rideaustin.test.setup;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.fixtures.RiderFixture;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;

@Component
public class C1176987Setup implements SetupAction<C1176987Setup> {

  @Inject
  private RiderFixture riderFixture;
  private Rider rider;

  @Inject
  private ActiveDriverFixtureProvider activeDriverFixtureProvider;
  private ActiveDriver activeDriver;

  @Override
  @Transactional
  public C1176987Setup setUp() throws Exception {
    this.rider = riderFixture.getFixture();
    this.activeDriver  = activeDriverFixtureProvider.create().getFixture();
    return this;
  }

  public Rider getRider() {
    return rider;
  }

  public ActiveDriver getActiveDriver() {
    return activeDriver;
  }
}
