package com.rideaustin.test.setup;

import javax.inject.Inject;
import javax.inject.Named;

import com.rideaustin.model.promocodes.Promocode;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.fixtures.CarFixture;
import com.rideaustin.test.fixtures.RiderFixture;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;
import com.rideaustin.test.fixtures.providers.DriverFixtureProvider;
import com.rideaustin.test.fixtures.providers.PromocodeFixtureProvider;

public abstract class AbstractSplitFarePromocodeApplicableToFeesSetup<T extends SetupAction<T>> implements SetupAction<T> {

  @Inject
  protected PromocodeFixtureProvider promocodeFixtureProvider;
  protected Promocode promocode;

  @Inject
  @Named("simpleRiderWithoutCharity")
  protected RiderFixture riderFixture;
  protected Rider rider;

  @Inject
  protected ActiveDriverFixtureProvider activeDriverFixtureProvider;
  protected ActiveDriver activeDriver;

  @Inject
  protected DriverFixtureProvider driverFixtureProvider;

  @Inject
  @Named("premiumCar")
  protected CarFixture carFixture;

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
