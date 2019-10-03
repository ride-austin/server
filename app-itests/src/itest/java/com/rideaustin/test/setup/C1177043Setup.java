package com.rideaustin.test.setup;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.promocodes.PromocodeRedemption;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.fixtures.CarFixture;
import com.rideaustin.test.fixtures.PromocodeRedemptionFixture;
import com.rideaustin.test.fixtures.RiderFixture;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;
import com.rideaustin.test.fixtures.providers.DriverFixtureProvider;
import com.rideaustin.test.fixtures.providers.PromocodeFixtureProvider;
import com.rideaustin.test.fixtures.providers.PromocodeRedemptionFixtureProvider;

@Component
public class C1177043Setup extends BasePromocodeTestSetup<C1177043Setup> {

  @Inject
  private ActiveDriverFixtureProvider activeDriverFixtureProvider;
  private ActiveDriver activeDriver;

  @Inject
  private DriverFixtureProvider driverFixtureProvider;
  @Inject
  @Named("suvCar")
  private CarFixture carFixture;

  @Inject
  private RiderFixture riderFixture;
  private Rider rider;

  @Inject
  private PromocodeRedemptionFixtureProvider redemptionFixtureProvider;
  private PromocodeRedemption redemption;

  @Inject
  private PromocodeFixtureProvider promocodeFixtureProvider;

  @Override
  @Transactional
  public C1177043Setup setUp() throws Exception {
    activeDriver = activeDriverFixtureProvider.create(driverFixtureProvider.create(carFixture)).getFixture();
    rider = riderFixture.getFixture();
    redemption = createRedemption(1).getFixture();
    return this;
  }

  private PromocodeRedemptionFixture createRedemption(Integer carTypeBitMask) {
    return redemptionFixtureProvider.create(
      builder -> builder
        .riderFixture(riderFixture)
        .promocodeFixture(promocodeFixtureProvider.create(
          20d, null,
          promoBuilder ->
            promoBuilder
              .cityBitMask(2)
              .carTypeBitMask(carTypeBitMask)
              .nextTripOnly(false)
              .maxUsePerAccount(10))));
  }

  public ActiveDriver getActiveDriver() {
    return activeDriver;
  }

  public Rider getRider() {
    return rider;
  }

  public PromocodeRedemption getRedemption() {
    return redemption;
  }
}
