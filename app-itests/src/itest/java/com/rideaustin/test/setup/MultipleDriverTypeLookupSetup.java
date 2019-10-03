package com.rideaustin.test.setup;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.test.fixtures.CarFixture;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;
import com.rideaustin.test.fixtures.providers.DriverFixtureProvider;

@Component
public class MultipleDriverTypeLookupSetup extends BaseWODispatchSetup<MultipleDriverTypeLookupSetup> {

  @Inject
  private ActiveDriverFixtureProvider activeDriverFixtureProvider;
  @Inject
  private DriverFixtureProvider driverFixtureProvider;
  @Inject
  @Named("regularCar")
  private CarFixture carFixture;

  @Override
  @Transactional
  public MultipleDriverTypeLookupSetup setUp() throws Exception {
    super.setUp();
    this.regularDriver = activeDriverFixtureProvider.create(driverFixtureProvider.create(carFixture, 6)).getFixture();
    return this;
  }

  @Override
  protected MultipleDriverTypeLookupSetup getThis() {
    return this;
  }

}
