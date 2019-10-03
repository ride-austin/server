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
import com.rideaustin.model.user.Rider;
import com.rideaustin.service.areaqueue.AreaQueueUpdateService;
import com.rideaustin.test.AbstractNonTxTests;
import com.rideaustin.test.actions.DriverAction;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.asserts.EventAssertHelper;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.config.TestSetupConfig;
import com.rideaustin.test.setup.C1183011Setup;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

@Category(AirportQueue.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class, TestSetupConfig.class}, initializers = RAApplicationInitializer.class)
public class C1183011IT extends AbstractNonTxTests<C1183011Setup> {

  @Inject
  private RiderAction riderAction;
  @Inject
  private DriverAction driverAction;

  @Inject
  private AreaQueueUpdateService queueService;

  @Inject
  private EventAssertHelper eventAssertHelper;

  private ActiveDriver regularDriver;
  private ActiveDriver suvDriver;
  private Rider rider;
  private LatLng airportLocation;
  private Area airport;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    setup = createSetup();
    regularDriver = setup.getRegularDriver();
    suvDriver = setup.getSuvDriver();
    rider = setup.getRider();
    airportLocation = locationProvider.getAirportLocation();
    airport = locationProvider.getAirport();
    appearInQueue(regularDriver, new String[]{TestUtils.REGULAR});
    appearInQueue(suvDriver, new String[]{TestUtils.REGULAR, TestUtils.SUV});
  }

  @Test
  @TestCases("C1183011")
  public void testRegular() throws Exception {
    doTest(TestUtils.REGULAR, regularDriver);
  }

  @Test
  @TestCases("C1183011")
  public void testSuv() throws Exception {
    doTest(TestUtils.SUV, suvDriver);
  }

  private void doTest(String category, ActiveDriver driver) throws Exception {
    Long ride = riderAction.requestRide(rider.getEmail(), airportLocation, category);

    awaitDispatch(driver, ride);
    driverAction.acceptRide(driver, ride);

    eventAssertHelper.assertLastEventIsSent(driver.getDriver(), EventType.REQUESTED);

    riderAction.cancelRide(rider.getEmail(), ride);
  }

  private void appearInQueue(ActiveDriver driver, String[] categories) throws Exception {
    driverAction
      .locationUpdate(driver, airportLocation.lat, airportLocation.lng, categories)
      .andExpect(status().isOk());
    queueService.updateStatuses(airport.getId());
    sleeper.sleep(500);
  }

}
