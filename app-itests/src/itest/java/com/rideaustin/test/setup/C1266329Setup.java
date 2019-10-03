package com.rideaustin.test.setup;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.test.fixtures.CarFixture;

@Component
public class C1266329Setup extends BaseWODispatchSetup<C1266329Setup> {

  @Inject
  @Named("allTypesCar")
  private CarFixture carFixture;

  @Override
  @Transactional
  public C1266329Setup setUp() throws Exception {
    super.setUp();
    woDriver = activeDriverFixtureProvider.create(driverFixtureProvider.create(carFixture, 1)).getFixture();
    return this;
  }

  @Override
  protected C1266329Setup getThis() {
    return this;
  }

}
