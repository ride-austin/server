package com.rideaustin.test.fixtures.providers;

import static com.rideaustin.Constants.City.AUSTIN;

import javax.persistence.EntityManager;

import com.rideaustin.repo.dsl.RiderDslRepository;
import com.rideaustin.test.fixtures.CardFixture;
import com.rideaustin.test.fixtures.CharityFixture;
import com.rideaustin.test.fixtures.RiderFixture;

public class RiderFixtureProvider {

  private final UserFixtureProvider userFixtureProvider;
  private final CardFixture cardFixture;
  private final CharityFixture charityFixture;
  private final RiderDslRepository riderDslRepository;
  private final EntityManager entityManager;

  public RiderFixtureProvider(UserFixtureProvider userFixtureProvider, CardFixture cardFixture,
    CharityFixture charityFixture, RiderDslRepository riderDslRepository, EntityManager entityManager) {
    this.userFixtureProvider = userFixtureProvider;
    this.cardFixture = cardFixture;
    this.charityFixture = charityFixture;
    this.riderDslRepository = riderDslRepository;
    this.entityManager = entityManager;
  }

  public RiderFixture create() {
    return create(charityFixture);
  }

  public RiderFixture create(CharityFixture charityFixture) {
    RiderFixture riderFixture = RiderFixture.builder()
      .userFixture(userFixtureProvider.create())
      .primaryCardFixture(cardFixture)
      .charityFixture(charityFixture)
      .cityId(AUSTIN.getId())
      .build();

    riderFixture.setRepository(riderDslRepository);
    riderFixture.setEntityManager(entityManager);
    return riderFixture;
  }
}
