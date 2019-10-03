package com.rideaustin.test.setup;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.fixtures.RiderFixture;
import com.rideaustin.test.fixtures.SurgeAreaFixture;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;

@Component
public class C1176988Setup extends BaseSurgeRedispatchTestSetup<C1176988Setup> {

  @Inject
  private RiderFixture riderFixture;
  private Rider rider;

  @Inject
  private ActiveDriverFixtureProvider activeDriverFixtureProvider;
  private ActiveDriver activeDriver;

  @Inject
  @Named("doubleSurgeAreaFixture")
  private SurgeAreaFixture surgeAreaFixture;

  @Override
  @Transactional
  public C1176988Setup setUp() throws Exception {
    this.rider = riderFixture.getFixture();
    this.surgeArea = surgeAreaFixture.getFixture();
    this.activeDriver  = activeDriverFixtureProvider.create().getFixture();
    return super.setUp();
  }

  @Override
  @Transactional
  protected C1176988Setup getThis() {
    return this;
  }

  @Override
  public Rider getRider() {
    return rider;
  }

  public ActiveDriver getActiveDriver() {
    return activeDriver;
  }
}
