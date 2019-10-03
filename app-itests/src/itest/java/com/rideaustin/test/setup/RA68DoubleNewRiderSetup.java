package com.rideaustin.test.setup;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.Constants;
import com.rideaustin.model.promocodes.PromocodeRedemption;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.fixtures.CarTypeFixture;
import com.rideaustin.test.fixtures.PromocodeFixture;
import com.rideaustin.test.fixtures.PromocodeRedemptionFixture;
import com.rideaustin.test.fixtures.RiderFixture;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;
import com.rideaustin.test.fixtures.providers.PromocodeFixtureProvider;
import com.rideaustin.test.fixtures.providers.PromocodeRedemptionFixtureProvider;

@Component
public class RA68DoubleNewRiderSetup extends BasePromocodeTestSetup<RA68DoubleNewRiderSetup> {

  @Inject
  private CarTypeFixture carTypeRegularFixture;

  @Inject
  private RiderFixture riderFixture;
  private Rider rider;

  @Inject
  private ActiveDriverFixtureProvider activeDriverFixtureProvider;
  private ActiveDriver activeDriver;

  @Inject
  private PromocodeRedemptionFixtureProvider redemptionFixtureProvider;
  private PromocodeRedemption firstRedemption;
  private PromocodeRedemption secondRedemption;

  @Inject
  private PromocodeFixtureProvider promocodeFixtureProvider;

  @Override
  @Transactional
  public RA68DoubleNewRiderSetup setUp() throws Exception {
    CarType regularCarType = carTypeRegularFixture.getFixture();
    this.rider = riderFixture.getFixture();
    this.activeDriver = activeDriverFixtureProvider.create().getFixture();

    final PromocodeFixture promocodeFixture = promocodeFixtureProvider.create(
      getMinimumFareForCityCarType(Constants.DEFAULT_CITY_ID, regularCarType).doubleValue(), null,
      promoBuilder ->
        promoBuilder
          .cityBitMask(null)
          .carTypeBitMask(null)
          .nextTripOnly(false)
          .maxUsePerAccount(1)
          .newRidersOnly(true)
    );
    PromocodeRedemptionFixture firstRedemptionFixture = redemptionFixtureProvider.create(
      builder -> builder
        .riderFixture(riderFixture)
        .promocodeFixture(promocodeFixture)
    );
    PromocodeRedemptionFixture secondRedemptionFixture = redemptionFixtureProvider.create(
      builder -> builder
        .riderFixture(riderFixture)
        .promocodeFixture(promocodeFixture)
    );

    firstRedemption = firstRedemptionFixture.getFixture();
    secondRedemption = secondRedemptionFixture.getFixture();
    return this;
  }

  public Rider getRider() {
    return rider;
  }

  public ActiveDriver getActiveDriver() {
    return activeDriver;
  }

  public PromocodeRedemption getFirstRedemption() {
    return firstRedemption;
  }

  public PromocodeRedemption getSecondRedemption() {
    return secondRedemption;
  }
}
