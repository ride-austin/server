package com.rideaustin.test.fixtures;

import org.springframework.data.domain.Page;

import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.Rider.RiderBuilder;
import com.rideaustin.repo.dsl.RiderDslRepository;
import com.rideaustin.rest.model.ListRidersParams;
import com.rideaustin.rest.model.PagingParams;

public class RiderFixture extends AbstractFixture<Rider> {

  private UserFixture userFixture;
  private CardFixture primaryCardFixture;
  private CardFixture secondaryCardFixture;
  private CharityFixture charityFixture;
  private long cityId;
  private RiderDslRepository repository;

  RiderFixture(UserFixture userFixture, CardFixture primaryCardFixture, CardFixture secondaryCardFixture, CharityFixture charityFixture, long cityId) {
    this.userFixture = userFixture;
    this.primaryCardFixture = primaryCardFixture;
    this.secondaryCardFixture = secondaryCardFixture;
    this.charityFixture = charityFixture;
    this.cityId = cityId;
  }

  public static RiderFixtureBuilder builder() {
    return new RiderFixtureBuilder();
  }

  @Override
  protected Rider createObject() {
    RiderBuilder riderBuilder = Rider.builder()
      .cityId(cityId);
    if (charityFixture != null) {
      riderBuilder.charity(charityFixture.getFixture());
    }
    Rider rider = riderBuilder.build();
    rider.setActive(true);
    return rider;
  }

  @Override
  public Rider getFixture() {
    Rider rider;
    ListRidersParams params = new ListRidersParams();
    params.setEmail(userFixture.getEmail());
    Page<Rider> riders = repository.findRiders(params, new PagingParams());
    if (riders.getContent().isEmpty()) {
      rider = createObject();
    } else {
      rider = riders.getContent().get(0);
    }
    userFixture.addAvatar(rider);
    rider.setUser(userFixture.getFixture());
    rider = entityManager.merge(rider);
    rider.getUser().getAvatars().clear();
    rider.getUser().addAvatar(rider);
    rider = entityManager.merge(rider);
    if (primaryCardFixture != null) {
      primaryCardFixture.setRider(rider);
      rider.setPrimaryCard(primaryCardFixture.getFixture());
    }
    if (secondaryCardFixture != null) {
      secondaryCardFixture.setRider(rider);
      secondaryCardFixture.getFixture();
    }
    rider = entityManager.merge(rider);
    entityManager.flush();
    return rider;
  }

  public void setRepository(RiderDslRepository repository) {
    this.repository = repository;
  }

  public static class RiderFixtureBuilder {
    private UserFixture userFixture;
    private CardFixture primaryCardFixture;
    private CardFixture secondaryCardFixture;
    private CharityFixture charityFixture;
    private long cityId;

    public RiderFixtureBuilder userFixture(UserFixture userFixture) {
      this.userFixture = userFixture;
      return this;
    }

    public RiderFixtureBuilder primaryCardFixture(CardFixture cardFixture) {
      this.primaryCardFixture = cardFixture;
      return this;
    }

    public RiderFixtureBuilder secondaryCardFixture(CardFixture cardFixture) {
      this.secondaryCardFixture = cardFixture;
      return this;
    }

    public RiderFixtureBuilder charityFixture(CharityFixture charityFixture) {
      this.charityFixture = charityFixture;
      return this;
    }

    public RiderFixtureBuilder cityId(long cityId) {
      this.cityId = cityId;
      return this;
    }

    public RiderFixture build() {
      return new RiderFixture(userFixture, primaryCardFixture, secondaryCardFixture, charityFixture, cityId);
    }

  }
}
