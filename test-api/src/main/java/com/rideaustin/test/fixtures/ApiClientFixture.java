package com.rideaustin.test.fixtures;

import com.rideaustin.model.user.ApiClient;
import com.rideaustin.repo.dsl.ApiClientDslRepository;

public class ApiClientFixture extends AbstractFixture<ApiClient> {

  private UserFixture userFixture;
  private ApiClientDslRepository repository;

  ApiClientFixture(UserFixture userFixture, ApiClientDslRepository repository) {
    this.userFixture = userFixture;
    this.repository = repository;
  }

  @Override
  protected ApiClient createObject() {
    return new ApiClient();
  }

  @Override
  public ApiClient getFixture() {
    ApiClient result;
    final ApiClient existing = repository.findByEmail(userFixture.getEmail());
    if (existing == null) {
      result = createObject();
    } else {
      result = existing;
    }
    userFixture.addAvatar(result);
    result.setUser(userFixture.getFixture());
    result = entityManager.merge(result);
    result.getUser().getAvatars().clear();
    result.getUser().addAvatar(result);
    result = entityManager.merge(result);
    entityManager.flush();
    return result;
  }

  public static ApiClientFixtureBuilder builder() {
    return new ApiClientFixtureBuilder();
  }

  public static class ApiClientFixtureBuilder {
    private UserFixture userFixture;
    private ApiClientDslRepository repository;

    public ApiClientFixtureBuilder userFixture(UserFixture userFixture) {
      this.userFixture = userFixture;
      return this;
    }

    public ApiClientFixtureBuilder repository(ApiClientDslRepository repository) {
      this.repository = repository;
      return this;
    }

    public ApiClientFixture build() {
      return new ApiClientFixture(userFixture, repository);
    }
  }
}
