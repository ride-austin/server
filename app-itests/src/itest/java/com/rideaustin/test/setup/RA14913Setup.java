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
public class RA14913Setup implements SetupAction<RA14913Setup> {

  @Inject
  private ActiveDriverFixtureProvider provider;
  private ActiveDriver activeDriver;

  @Inject
  private RiderFixtureProvider riderFixtureProvider;
  private List<Rider> riders = new ArrayList<>();

  @Override
  @Transactional
  public RA14913Setup setUp() throws Exception {
    activeDriver = provider.create().getFixture();
    for (int i = 0; i < 10; i++) {
      riders.add(riderFixtureProvider.create().getFixture());
    }
    return this;
  }

  public ActiveDriver getActiveDriver() {
    return activeDriver;
  }

  public List<Rider> getRiders() {
    return riders;
  }
}
