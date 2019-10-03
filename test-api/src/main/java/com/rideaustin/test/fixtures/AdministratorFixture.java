package com.rideaustin.test.fixtures;

import com.rideaustin.model.user.Administrator;

public class AdministratorFixture extends AbstractFixture<Administrator> {

  private UserFixture userFixture;

  AdministratorFixture(UserFixture userFixture) {
    this.userFixture = userFixture;

  }

  public static AdministratorFixtureBuilder builder() {
    return new AdministratorFixtureBuilder();
  }

  @Override
  protected Administrator createObject() {
    Administrator administrator = Administrator.builder()
      .build();
    administrator.setActive(true);
    return administrator;
  }

  @Override
  public Administrator getFixture() {
    Administrator administrator = createObject();
    userFixture.addAvatar(administrator);
    administrator.setUser(userFixture.getFixture());
    administrator = entityManager.merge(administrator);
    entityManager.flush();
    return administrator;
  }

  public static class AdministratorFixtureBuilder {
    private UserFixture userFixture;

    public AdministratorFixture.AdministratorFixtureBuilder userFixture(UserFixture userFixture) {
      this.userFixture = userFixture;
      return this;
    }

    public AdministratorFixture build() {
      return new AdministratorFixture(userFixture);
    }

  }
}