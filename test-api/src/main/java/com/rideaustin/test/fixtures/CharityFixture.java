package com.rideaustin.test.fixtures;

import com.rideaustin.model.Charity;

public class CharityFixture extends AbstractFixture<Charity> {

  private boolean createCharity;

  @java.beans.ConstructorProperties({"createCharity"})
  CharityFixture(boolean createCharity) {
    this.createCharity = createCharity;
  }

  public static CharityFixtureBuilder builder() {
    return new CharityFixtureBuilder();
  }

  @Override
  protected Charity createObject() {
    if (!createCharity) {
      return null;
    }
    return entityManager.find(Charity.class, 1L);
  }

  public static class CharityFixtureBuilder {
    private boolean createCharity;


    public CharityFixture.CharityFixtureBuilder createCharity(boolean createCharity) {
      this.createCharity = createCharity;
      return this;
    }

    public CharityFixture build() {
      return new CharityFixture(createCharity);
    }

  }
}
