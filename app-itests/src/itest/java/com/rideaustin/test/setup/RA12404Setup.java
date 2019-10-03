package com.rideaustin.test.setup;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.promocodes.Promocode;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.fixtures.PromocodeFixture;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;
import com.rideaustin.test.fixtures.providers.RiderFixtureProvider;

@Component
public class RA12404Setup implements SetupAction<RA12404Setup> {

  private Rider mainRider;
  private Rider secondRider;

  @Inject
  private RiderFixtureProvider riderFixtureProvider;

  @Inject
  @Named("applicableToFeesPromocode")
  private PromocodeFixture applicableToFeesFixture;
  private Promocode promocode;

  @Inject
  private ActiveDriverFixtureProvider activeDriverFixtureProvider;
  private ActiveDriver activeDriver;

  @Override
  @Transactional
  public RA12404Setup setUp() throws Exception {
    mainRider = riderFixtureProvider.create().getFixture();
    secondRider = riderFixtureProvider.create().getFixture();
    promocode = applicableToFeesFixture.getFixture();
    activeDriver = activeDriverFixtureProvider.create().getFixture();
    return this;
  }

  public Rider getMainRider() {
    return mainRider;
  }

  public Rider getSecondRider() {
    return secondRider;
  }

  public Promocode getPromocode() {
    return promocode;
  }

  public ActiveDriver getActiveDriver() {
    return activeDriver;
  }
}
