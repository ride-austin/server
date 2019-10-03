package com.rideaustin.test.setup;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.user.Rider;
import com.rideaustin.test.fixtures.RiderFixture;

@Component
public class RA12777Setup extends BaseApplePaySetup<RA12777Setup> {

  @Inject
  @Named("riderWithoutCard")
  private RiderFixture noCardRiderFixture;
  private Rider noCardRider;

  @Override
  @Transactional
  public RA12777Setup setUp() throws Exception {
    super.setUp();
    noCardRider = noCardRiderFixture.getFixture();
    return this;
  }

  @Override
  protected RA12777Setup getThis() {
    return this;
  }

  public Rider getNoCardRider() {
    return noCardRider;
  }
}
