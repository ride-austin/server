package com.rideaustin.dispatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import com.google.maps.model.LatLng;
import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.test.AbstractNonTxTests;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.config.TestSetupConfig;
import com.rideaustin.test.setup.C1177112Setup;

@TestPropertySource(properties = "map.api.default.provider=google")
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class, TestSetupConfig.class}, initializers = RAApplicationInitializer.class)
public class RA137GooglePlaceIT extends AbstractNonTxTests<C1177112Setup> {

  @Inject
  private RiderAction riderAction;
  @Inject
  private RideDslRepository repository;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    setup = createSetup();
  }

  @Test
  public void testGooglePlaceOverridesAddress() throws Exception {
    final LatLng location = locationProvider.getCenter();

    final Long rideId = riderAction.requestRide(setup.getRider().getEmail(), "ChIJzxUS9Z61RIYRopUORuC6AdM", location);

    awaitStatus(rideId, RideStatus.NO_AVAILABLE_DRIVER);

    assertEquals("1800 Congress Ave, Austin", repository.findOne(rideId).getStart().getAddress());
  }

  @Test
  public void testPickupPointAddsToAddress() throws Exception {
    final LatLng location = new LatLng(30.20446, -97.66649);

    final Long rideId = riderAction.requestRide(setup.getRider().getEmail(), location);

    awaitStatus(rideId, RideStatus.NO_AVAILABLE_DRIVER);

    assertTrue(repository.findOne(rideId).getStart().getAddress().endsWith("(Yellow 4)"));
  }
}
