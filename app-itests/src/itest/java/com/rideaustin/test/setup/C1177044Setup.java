package com.rideaustin.test.setup;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.promocodes.PromocodeRedemption;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.fixtures.CarTypeFixture;
import com.rideaustin.test.fixtures.PromocodeRedemptionFixture;
import com.rideaustin.test.fixtures.RiderFixture;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;
import com.rideaustin.test.fixtures.providers.PromocodeFixtureProvider;
import com.rideaustin.test.fixtures.providers.PromocodeRedemptionFixtureProvider;

@Component
public class C1177044Setup extends BasePromocodeTestSetup<C1177044Setup> {

  @Inject
  private CarTypeFixture carTypeRegularFixture;
  private CarType regularCarType;

  @Inject
  private RiderFixture riderFixture;
  private Rider rider;

  @Inject
  private ActiveDriverFixtureProvider activeDriverFixtureProvider;
  private ActiveDriver activeDriver;

  @Inject
  private PromocodeRedemptionFixtureProvider redemptionFixtureProvider;
  private PromocodeRedemption redemption;

  @Inject
  private PromocodeFixtureProvider promocodeFixtureProvider;

  @Override
  @Transactional
  public C1177044Setup setUp() throws Exception {
    this.regularCarType = carTypeRegularFixture.getFixture();
    this.rider = riderFixture.getFixture();
    this.activeDriver = activeDriverFixtureProvider.create().getFixture();

    PromocodeRedemptionFixture redemptionFixture = redemptionFixtureProvider.create(
      builder -> builder
        .riderFixture(riderFixture)
        .promocodeFixture(promocodeFixtureProvider.create(
          20.0, null,
          promoBuilder ->
            promoBuilder
              .cityBitMask(null)
              .carTypeBitMask(null)
              .nextTripOnly(true)
              .maxUsePerAccount(10))));

    redemption = redemptionFixture.getFixture();
    return this;
  }

  public Rider getRider() {
    return rider;
  }

  public ActiveDriver getActiveDriver() {
    return activeDriver;
  }

  public PromocodeRedemption getRedemption() {
    return redemption;
  }
}
