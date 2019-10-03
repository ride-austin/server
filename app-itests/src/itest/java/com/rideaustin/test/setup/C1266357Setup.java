package com.rideaustin.test.setup;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.Session;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.fixtures.CarFixture;
import com.rideaustin.test.fixtures.RiderFixture;
import com.rideaustin.test.fixtures.SessionFixture;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;
import com.rideaustin.test.fixtures.providers.DriverFixtureProvider;

@Component
public class C1266357Setup implements SetupAction<C1266357Setup> {

  @Inject
  private RiderFixture riderFixture;
  @Inject
  private ActiveDriverFixtureProvider activeDriverFixtureProvider;
  @Inject
  private DriverFixtureProvider driverFixtureProvider;
  @Inject
  @Named("regularCar")
  private CarFixture carFixture;
  @Inject
  @Named("app320Session")
  private SessionFixture sessionFixture;

  private Session riderSession;
  private Rider rider;
  private ActiveDriver activeDriver;


  @Override
  @Transactional
  public C1266357Setup setUp() throws Exception {
    rider = riderFixture.getFixture();
    activeDriver = activeDriverFixtureProvider.create(driverFixtureProvider.create(carFixture, 1)).getFixture();
    riderSession = sessionFixture.getFixture();
    return this;
  }

  public Session getRiderSession() {
    return riderSession;
  }

  public Rider getRider() {
    return rider;
  }

  public ActiveDriver getActiveDriver() {
    return activeDriver;
  }
}
