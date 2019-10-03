package com.rideaustin.test.setup;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;

import com.rideaustin.test.fixtures.CarFixture;

@Component
public class C1177095Setup extends BaseDriverStatusManagementSetup<C1177095Setup> {

  @Inject
  @Named("premiumCar")
  private CarFixture carFixture;

  @Override
  protected C1177095Setup doSetup() {
    this.activeDriver = activeDriverFixtureProvider.create(driverFixtureProvider.create(carFixture, 1)).getFixture();
    return this;
  }
}
