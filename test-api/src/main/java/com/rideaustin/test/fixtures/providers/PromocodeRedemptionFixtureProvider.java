package com.rideaustin.test.fixtures.providers;

import java.util.function.Consumer;

import javax.persistence.EntityManager;

import com.rideaustin.test.fixtures.PromocodeFixture;
import com.rideaustin.test.fixtures.PromocodeRedemptionFixture;
import com.rideaustin.test.fixtures.PromocodeRedemptionFixture.PromocodeRedemptionFixtureBuilder;
import com.rideaustin.test.fixtures.RiderFixture;

public class PromocodeRedemptionFixtureProvider {

  private final RiderFixture riderFixture;

  private final PromocodeFixture promocodeFixture;

  private final EntityManager entityManager;

  public PromocodeRedemptionFixtureProvider(RiderFixture riderFixture, PromocodeFixture promocodeFixture,
    EntityManager entityManager) {
    this.riderFixture = riderFixture;
    this.promocodeFixture = promocodeFixture;
    this.entityManager = entityManager;
  }

  public PromocodeRedemptionFixture create() {
    return create(builder -> {
    });
  }

  public PromocodeRedemptionFixture create(Consumer<PromocodeRedemptionFixtureBuilder> postProcessor) {
    PromocodeRedemptionFixtureBuilder fixtureBuilder = PromocodeRedemptionFixture.builder()
      .active(true)
      .valid(true)
      .promocodeFixture(promocodeFixture)
      .riderFixture(riderFixture);
    postProcessor.accept(fixtureBuilder);

    PromocodeRedemptionFixture redemptionFixture = fixtureBuilder.build();
    redemptionFixture.setEntityManager(entityManager);
    return redemptionFixture;
  }
}
