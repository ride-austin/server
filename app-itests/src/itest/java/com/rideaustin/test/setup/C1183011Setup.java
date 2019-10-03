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
public class C1183011Setup implements SetupAction<C1183011Setup> {

  @Inject
  private ActiveDriverFixtureProvider provider;

  @Inject
  private DriverFixtureProvider driverFixtureProvider;

  @Inject
  @Named("regularCar")
  private CarFixture regularCar;

  @Inject
  @Named("suvCar")
  private CarFixture suvCar;

  @Inject
  private RiderFixture riderFixture;

  private ActiveDriver regularDriver;
  private Rider rider;
  private ActiveDriver suvDriver;

  @Override
  @Transactional
  public C1183011Setup setUp() throws Exception {
    rider = riderFixture.getFixture();
    regularDriver = provider.create(driverFixtureProvider.create(regularCar)).getFixture();
    suvDriver = provider.create(driverFixtureProvider.create(suvCar)).getFixture();
    return this;
  }

  public ActiveDriver getRegularDriver() {
    return regularDriver;
  }

  public Rider getRider() {
    return rider;
  }

  public ActiveDriver getSuvDriver() {
    return suvDriver;
  }
}
