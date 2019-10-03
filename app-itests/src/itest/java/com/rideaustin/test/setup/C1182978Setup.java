package com.rideaustin.test.setup;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.user.Rider;
import com.rideaustin.test.fixtures.providers.RiderFixtureProvider;

@Component
public class C1182978Setup extends BaseRedispatchTestSetup<C1182978Setup> {

  @Inject
  private RiderFixtureProvider riderFixtureProvider;
  private Rider secondRider;
  private Rider thirdRider;

  @Override
  @Transactional
  public C1182978Setup setUp() throws Exception {
    C1182978Setup setup = super.setUp();
    secondRider = riderFixtureProvider.create().getFixture();
    thirdRider = riderFixtureProvider.create().getFixture();
    return setup;
  }

  @Override
  protected C1182978Setup getThis() {
    return this;
  }

  public Rider getSecondRider() {
    return secondRider;
  }

  public Rider getThirdRider() {
    return thirdRider;
  }
}
