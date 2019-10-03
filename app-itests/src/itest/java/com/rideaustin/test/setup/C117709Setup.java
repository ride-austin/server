package com.rideaustin.test.setup;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.user.Rider;
import com.rideaustin.test.fixtures.RiderFixture;

@Component
public class C117709Setup implements SetupAction<C117709Setup> {

  @Inject
  private RiderFixture riderFixture;
  private Rider rider;

  @Override
  @Transactional
  public C117709Setup setUp() throws Exception {
    this.rider = riderFixture.getFixture();
    return this;
  }

  public Rider getRider() {
    return rider;
  }
}
