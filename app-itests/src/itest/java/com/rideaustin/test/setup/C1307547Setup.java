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
public class C1307547Setup implements SetupAction<C1307547Setup> {

  @Inject
  @Named("fixedAmountPromocode")
  private PromocodeFixture promocodeFixture;
  private Promocode promocode;

  @Inject
  private RiderFixture riderFixture;
  private Rider rider;

  @Inject
  private ActiveDriverFixtureProvider activeDriverFixtureProvider;
  private ActiveDriver activeDriver;

  @Override
  @Transactional
  public C1307547Setup setUp() throws Exception {
    this.promocode = promocodeFixture.getFixture();
    this.rider = riderFixture.getFixture();
    this.activeDriver = activeDriverFixtureProvider.create().getFixture();
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
