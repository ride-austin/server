package com.rideaustin.test.setup;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;

@Component
public class C1183004Setup implements SetupAction<C1183004Setup> {

  @Inject
  private ActiveDriverFixtureProvider activeDriverFixtureProvider;
  private ActiveDriver availableActiveDriver;

  @Override
  @Transactional
  public C1183004Setup setUp() throws Exception {
    availableActiveDriver = activeDriverFixtureProvider.create().getFixture();
    return this;
  }

  public ActiveDriver getAvailableActiveDriver() {
    return availableActiveDriver;
  }
}
