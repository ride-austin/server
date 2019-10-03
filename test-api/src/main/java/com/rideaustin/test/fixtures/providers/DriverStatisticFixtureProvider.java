package com.rideaustin.test.fixtures.providers;

import javax.persistence.EntityManager;

import com.rideaustin.test.fixtures.DriverStatisticFixture;

public class DriverStatisticFixtureProvider {

  private final EntityManager entityManager;

  public DriverStatisticFixtureProvider(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  public DriverStatisticFixture create(long driverId, int lastAccepted, int lastAcceptedOver, int lastCancelled, int lastCancelledOver) {
    DriverStatisticFixture fixture = new DriverStatisticFixture(driverId, lastAccepted, lastAcceptedOver, lastCancelled, lastCancelledOver);
    fixture.setEntityManager(entityManager);
    return fixture;
  }

  public DriverStatisticFixture create(long driverId) {
    return create(driverId, 0, 0, 0, 0);
  }
}
