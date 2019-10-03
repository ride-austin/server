package com.rideaustin.test.setup;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.test.fixtures.CarFixture;

@Component
public class C1182972Setup extends BaseRedispatchTestSetup<C1182972Setup> {

  @Inject
  @Named("premiumCar")
  protected CarFixture carFixture;

  @Override
  @Transactional
  public C1182972Setup setUp() throws Exception {
    firstActiveDriver = activeDriverFixtureProvider.create(driverFixtureProvider.create(carFixture, 1)).getFixture();
    secondActiveDriver = activeDriverFixtureProvider.create(driverFixtureProvider.create(carFixture, 1)).getFixture();
    rider = riderFixture.getFixture();
    return this;
  }

  @Override
  protected C1182972Setup getThis() {
    return this;
  }
}
