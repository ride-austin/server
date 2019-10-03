package com.rideaustin.test.fixtures.providers;

import javax.persistence.EntityManager;

import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.test.fixtures.ActiveDriverFixture;
import com.rideaustin.test.fixtures.CarFixture;
import com.rideaustin.test.fixtures.DriverFixture;

public class ActiveDriverFixtureProvider {

  private final EntityManager entityManager;
  private final DriverFixtureProvider driverFixtureProvider;
  private final CarFixture defaultCarFixture;

  public ActiveDriverFixtureProvider(EntityManager entityManager, DriverFixtureProvider driverFixtureProvider, CarFixture defaultCarFixture) {
    this.entityManager = entityManager;
    this.driverFixtureProvider = driverFixtureProvider;
    this.defaultCarFixture = defaultCarFixture;
  }

  public ActiveDriverFixture create() {
    return create(driverFixtureProvider.create(defaultCarFixture));
  }

  public ActiveDriverFixture create(DriverFixture driverFixture) {
    ActiveDriverFixture fixture = ActiveDriverFixture.builder()
      .status(ActiveDriverStatus.AVAILABLE)
      .driverFixture(driverFixture)
      .build();
    fixture.setEntityManager(entityManager);
    return fixture;
  }

}
