package com.rideaustin.test.setup;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.rideaustin.test.fixtures.CarFixture;

@Component
public class C1177093Setup extends BaseDriverStatusManagementSetup<C1177093Setup> {

  @Inject
  private CarFixture regularCar;

  @Override
  protected C1177093Setup doSetup() {
    this.activeDriver = activeDriverFixtureProvider.create(driverFixtureProvider.create(regularCar)).getFixture();
    return this;
  }
}
