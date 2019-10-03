package com.rideaustin.queue;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import com.google.maps.model.LatLng;
import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.model.Area;
import com.rideaustin.model.enums.EventType;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Driver;
import com.rideaustin.service.areaqueue.AreaQueueUpdateService;
import com.rideaustin.test.AbstractNonTxTests;
import com.rideaustin.test.actions.DriverAction;
import com.rideaustin.test.asserts.EventAssertHelper;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.config.TestSetupConfig;
import com.rideaustin.test.setup.C1183004Setup;

@Category(AirportQueue.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class, TestSetupConfig.class}, initializers = RAApplicationInitializer.class)
public class C1183004LeaveIT extends AbstractNonTxTests<C1183004Setup> {

  @Inject
  private DriverAction driverAction;
  @Inject
  private AreaQueueUpdateService queueService;
  @Inject
  private EventAssertHelper eventAssertHelper;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    setup = createSetup();
  }

  @Test
  public void testLeave() throws Exception {
    ActiveDriver availableActiveDriver = setup.getAvailableActiveDriver();
    Driver driver = availableActiveDriver.getDriver();
    String email = driver.getEmail();

    LatLng airportLocation = locationProvider.getAirportLocation();
    driverAction
      .locationUpdate(availableActiveDriver, airportLocation.lat, airportLocation.lng)
      .andExpect(status().isOk());
    Area airport = locationProvider.getAirport();
    queueService.updateStatuses(airport.getId());

    driverAction
      .goOffline(email)
      .andExpect(status().isOk());

    long timeoutSeconds = environment.getProperty("area.inactive.time_before_leave.in_minutes", Long.class, 1L)*60;
    sleeper.sleep((timeoutSeconds + 1) * 1000);

    queueService.updateStatuses(airport.getId());
    eventAssertHelper.assertLastEventIsSent(driver, EventType.QUEUED_AREA_LEAVING_INACTIVE);
  }

}
