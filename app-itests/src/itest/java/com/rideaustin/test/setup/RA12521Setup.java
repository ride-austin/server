package com.rideaustin.test.setup;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.promocodes.Promocode;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.fixtures.PromocodeFixture;
import com.rideaustin.test.fixtures.RiderFixture;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;

@Component
public class RA12521Setup implements SetupAction<RA12521Setup> {

  @Inject
  @Named("applicableToFeesPromocode")
  protected PromocodeFixture applicableToFeesFixture;
  protected Promocode promocode;

  @Inject
  private RiderFixture riderFixture;
  protected Rider rider;

  @Inject
  private ActiveDriverFixtureProvider activeDriverFixtureProvider;
  protected ActiveDriver activeDriver;

  @Override
  @Transactional
  public RA12521Setup setUp() throws Exception {
    this.rider = riderFixture.getFixture();
    this.activeDriver = activeDriverFixtureProvider.create().getFixture();
    this.promocode = applicableToFeesFixture.getFixture();
    return this;
  }

  public Promocode getPromocode() {
    return promocode;
  }

  public Rider getRider() {
    return rider;
  }

  public ActiveDriver getActiveDriver() {
    return activeDriver;
  }
}
