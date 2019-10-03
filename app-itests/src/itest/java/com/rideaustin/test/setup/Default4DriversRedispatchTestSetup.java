package com.rideaustin.test.setup;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.ride.ActiveDriver;

@Component
public class Default4DriversRedispatchTestSetup extends BaseRedispatchTestSetup<Default4DriversRedispatchTestSetup> {

  private ActiveDriver thirdActiveDriver;
  private ActiveDriver fourthActiveDriver;

  @Override
  @Transactional
  public Default4DriversRedispatchTestSetup setUp() throws Exception {
    Default4DriversRedispatchTestSetup result = super.setUp();
    thirdActiveDriver = createDriver();
    fourthActiveDriver = createDriver();
    return result;
  }

  @Override
  protected Default4DriversRedispatchTestSetup getThis() {
    return this;
  }

  public ActiveDriver getThirdActiveDriver() {
    return thirdActiveDriver;
  }

  public ActiveDriver getFourthActiveDriver() {
    return fourthActiveDriver;
  }
}
