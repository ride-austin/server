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
public class UpfrontPricingPromocodeSetup implements SetupAction<UpfrontPricingPromocodeSetup> {

  @Inject
  private ActiveDriverFixtureProvider provider;
  @Inject
  private RiderFixtureProvider riderFixtureProvider;
  @Inject
  @Named("applicableToFeesPromocode")
  protected PromocodeFixture applicableToFeesFixture;

  private Promocode promocode;
  private Rider rider;
  private ActiveDriver activeDriver;

  @Override
  @Transactional
  public UpfrontPricingPromocodeSetup setUp() throws Exception {
    promocode = applicableToFeesFixture.getFixture();
    rider = riderFixtureProvider.create().getFixture();
    activeDriver = provider.create().getFixture();
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
