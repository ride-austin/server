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
public class DefaultApplicableToFeesPromocodeSetup implements SetupAction<DefaultApplicableToFeesPromocodeSetup> {

  @Inject
  @Named("nonApplicableToFeesPromocode")
  protected PromocodeFixture nonApplicableToFeesFixture;

  @Inject
  @Named("applicableToFeesPromocode")
  protected PromocodeFixture applicableToFeesFixture;
  protected Promocode applicablePromocode;
  protected Promocode nonApplicablePromocode;

  @Inject
  private RiderFixtureProvider riderFixtureProvider;
  protected Rider rider;

  @Inject
  private ActiveDriverFixtureProvider activeDriverFixtureProvider;
  protected ActiveDriver activeDriver;

  @Override
  @Transactional
  public DefaultApplicableToFeesPromocodeSetup setUp() throws Exception {
    applicablePromocode = applicableToFeesFixture.getFixture();
    nonApplicablePromocode = nonApplicableToFeesFixture.getFixture();
    rider = riderFixtureProvider.create().getFixture();
    activeDriver = activeDriverFixtureProvider.create().getFixture();
    return this;
  }

  public Promocode getApplicablePromocode() {
    return applicablePromocode;
  }

  public Promocode getNonApplicablePromocode() {
    return nonApplicablePromocode;
  }

  public Rider getRider() {
    return rider;
  }

  public ActiveDriver getActiveDriver() {
    return activeDriver;
  }
}
