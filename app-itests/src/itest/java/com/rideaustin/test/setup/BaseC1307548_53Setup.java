package com.rideaustin.test.setup;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;

import com.rideaustin.model.promocodes.Promocode;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.fixtures.PromocodeFixture;
import com.rideaustin.test.fixtures.RiderFixture;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;

@Component
public abstract class BaseC1307548_53Setup<T extends BaseC1307548_53Setup> implements SetupAction<T> {

  @Inject
  @Named("fixedAmountPromocode")
  protected PromocodeFixture promocodeFixture;
  protected Promocode promocode;

  @Inject
  protected RiderFixture riderFixture;
  protected Rider rider;

  @Inject
  protected ActiveDriverFixtureProvider activeDriverFixtureProvider;
  protected ActiveDriver activeDriver;

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
