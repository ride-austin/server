package com.rideaustin.dispatch;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import com.rideaustin.Constants;
import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.AbstractNonTxTests;
import com.rideaustin.test.actions.DriverAction;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.actions.cascaded.CascadedDriverAction;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.config.TestSetupConfig;
import com.rideaustin.test.setup.RA99LimitDestinationUpdatesSetup;

@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class, TestSetupConfig.class}, initializers = RAApplicationInitializer.class)
public class RA99LimitDestinationUpdatesIT extends AbstractNonTxTests<RA99LimitDestinationUpdatesSetup> {

  private static final int UPDATE_LIMIT = 3;
  @Inject
  private RiderAction riderAction;
  @Inject
  private DriverAction driverAction;

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
    setup = createSetup();
    configurationItemCache.setBooleanConfig(Constants.DEFAULT_CITY_ID, ClientType.CONSOLE, "destinationUpdate", "enabled", true);
    configurationItemCache.setIntConfig(Constants.DEFAULT_CITY_ID, ClientType.CONSOLE, "destinationUpdate", "limit", UPDATE_LIMIT);
  }

  @Test
  public void test() throws Exception {
    final ActiveDriver activeDriver = setup.getActiveDriver();
    driverAction.goOnline(activeDriver.getDriver().getEmail(), locationProvider.getCenter());

    final Rider rider = setup.getRider();
    final Long rideId = riderAction.requestRide(rider.getEmail(), locationProvider.getCenter());

    awaitDispatch(activeDriver, rideId);

    new CascadedDriverAction(activeDriver.getDriver(), driverAction)
      .withRideId(rideId)
      .acceptRide()
      .reach()
      .startRide();

    for (int i = 0; i < UPDATE_LIMIT; i++) {
      riderAction.updateDestination(rider.getEmail(), rideId, locationProvider.getRandomLocation())
        .andExpect(status().isOk());
    }

    riderAction.updateDestination(rider.getEmail(), rideId, locationProvider.getRandomLocation())
      .andDo(print())
      .andExpect(status().isBadRequest());
  }
}
