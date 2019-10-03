package com.rideaustin.test.setup;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.ride.ActiveDriver;

@Component
public class C1181743Setup extends BaseApplePaySetup<C1181743Setup> {

  private ActiveDriver secondDriver;

  @Override
  @Transactional
  public C1181743Setup setUp() throws Exception {
    super.setUp();
    this.secondDriver = activeDriverFixtureProvider.create(driverFixtureProvider.create(carFixture)).getFixture();
    return this;
  }

  @Override
  protected C1181743Setup getThis() {
    return this;
  }

  public ActiveDriver getFirstDriver() {
    return super.getActiveDriver();
  }

  public ActiveDriver getSecondDriver() {
    return secondDriver;
  }
}
