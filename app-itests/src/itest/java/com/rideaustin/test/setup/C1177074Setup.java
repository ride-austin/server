package com.rideaustin.test.setup;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;

@Component
public class C1177074Setup extends AbstractRidesInQueueSetup<C1177074Setup> {

  @Inject
  private ActiveDriverFixtureProvider provider;

  private ActiveDriver driver1;
  private ActiveDriver driver2;
  private ActiveDriver driver3;

  @Override
  protected C1177074Setup doSetup() {
    this.driver1 = provider.create().getFixture();
    this.driver2 = provider.create().getFixture();
    this.driver3 = provider.create().getFixture();
    return this;
  }

  public ActiveDriver getDriver1() {
    return driver1;
  }

  public ActiveDriver getDriver2() {
    return driver2;
  }

  public ActiveDriver getDriver3() {
    return driver3;
  }
}
