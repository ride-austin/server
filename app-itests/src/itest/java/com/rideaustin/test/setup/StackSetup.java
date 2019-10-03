package com.rideaustin.test.setup;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;
import com.rideaustin.test.fixtures.providers.RiderFixtureProvider;

@Component
public class StackSetup implements SetupAction<StackSetup> {

  @Inject
  private ActiveDriverFixtureProvider provider;
  private List<ActiveDriver> activeDrivers = new ArrayList<>();

  @Inject
  private RiderFixtureProvider riderFixtureProvider;
  private List<Rider> riders = new ArrayList<>();

  @Override
  @Transactional
  public StackSetup setUp() throws Exception {
    for (int i = 0; i < 5; i++) {
      activeDrivers.add(provider.create().getFixture());
      riders.add(riderFixtureProvider.create().getFixture());
    }
    riders.add(riderFixtureProvider.create().getFixture());
    return this;
  }

  public List<ActiveDriver> getActiveDrivers() {
    return activeDrivers;
  }

  public List<Rider> getRiders() {
    return riders;
  }
}
