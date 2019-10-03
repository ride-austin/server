package com.rideaustin.test.setup;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.fixtures.CarFixture;
import com.rideaustin.test.fixtures.RiderFixture;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;
import com.rideaustin.test.fixtures.providers.DriverFixtureProvider;

@Component
public class C1177112Setup implements SetupAction<C1177112Setup> {

  @Inject
  protected RiderFixture riderFixture;

  @Inject
  protected ActiveDriverFixtureProvider activeDriverFixtureProvider;

  @Inject
  protected DriverFixtureProvider driverFixtureProvider;

  @Inject
  @Named("suvCar")
  protected CarFixture suvCarFixture;

  @Inject
  @Named("regularCar")
  protected CarFixture regularCarFixture;

  protected Rider rider;
  private ActiveDriver suvCarDriver;
  private ActiveDriver regularCarDriver;

  @Override
  @Transactional
  public C1177112Setup setUp() throws Exception {
    this.rider = riderFixture.getFixture();
    this.suvCarDriver = activeDriverFixtureProvider.create(driverFixtureProvider.create(suvCarFixture)).getFixture();
    this.regularCarDriver = activeDriverFixtureProvider.create(driverFixtureProvider.create(regularCarFixture)).getFixture();
    return this;
  }

  public Rider getRider() {
    return rider;
  }

  public ActiveDriver getSuvCarDriver() {
    return suvCarDriver;
  }

  public ActiveDriver getRegularCarDriver() {
    return regularCarDriver;
  }
}
