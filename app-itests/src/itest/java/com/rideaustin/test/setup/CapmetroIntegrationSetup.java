package com.rideaustin.test.setup;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.ApiClient;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.fixtures.ApiClientFixture;
import com.rideaustin.test.fixtures.RiderFixture;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;

@Component
public class CapmetroIntegrationSetup implements SetupAction<CapmetroIntegrationSetup> {

  @Inject
  private ApiClientFixture apiClientFixture;
  private ApiClient apiClient;

  @Inject
  private ActiveDriverFixtureProvider activeDriverFixtureProvider;
  private ActiveDriver activeDriver;

  @Inject
  private RiderFixture riderFixture;
  private Rider rider;

  @Override
  @Transactional
  public CapmetroIntegrationSetup setUp() throws Exception {
    apiClient = apiClientFixture.getFixture();
    activeDriver = activeDriverFixtureProvider.create().getFixture();
    rider = riderFixture.getFixture();
    return this;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public ActiveDriver getActiveDriver() {
    return activeDriver;
  }

  public Rider getRider() {
    return rider;
  }
}
