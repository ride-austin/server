package com.rideaustin.test.fixtures.providers;

import java.util.function.Consumer;

import javax.persistence.EntityManager;

import com.rideaustin.test.fixtures.PromocodeFixture;
import com.rideaustin.test.fixtures.PromocodeFixture.PromocodeFixtureBuilder;
import com.rideaustin.test.fixtures.RiderFixture;

public class PromocodeFixtureProvider {

  private final RiderFixture riderFixture;

  private final EntityManager entityManager;

  public PromocodeFixtureProvider(RiderFixture riderFixture, EntityManager entityManager) {
    this.riderFixture = riderFixture;
    this.entityManager = entityManager;
  }

  public PromocodeFixture create(double value) {
    return create(value, riderFixture);
  }

  public PromocodeFixture create(double value, RiderFixture riderFixture) {
    return create(value, riderFixture, builder -> {});
  }

  public PromocodeFixture create(double value, RiderFixture riderFixture, Consumer<PromocodeFixtureBuilder> postProcessor) {
    PromocodeFixtureBuilder fixtureBuilder = PromocodeFixture.builder()
      .value(value)
      .riderFixture(riderFixture);
    postProcessor.accept(fixtureBuilder);

    PromocodeFixture promocodeFixture = fixtureBuilder.build();
    promocodeFixture.setEntityManager(entityManager);
    return promocodeFixture;
  }
}
