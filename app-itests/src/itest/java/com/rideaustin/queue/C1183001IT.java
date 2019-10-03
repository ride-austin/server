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
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.service.areaqueue.AreaQueueUpdateService;
import com.rideaustin.test.AbstractNonTxTests;
import com.rideaustin.test.actions.DriverAction;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.actions.cascaded.CascadedDriverAction;
import com.rideaustin.test.asserts.DriverQueueAssert;
import com.rideaustin.test.asserts.EventAssertHelper;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.config.TestSetupConfig;
import com.rideaustin.test.response.AreaQueuePositions;
import com.rideaustin.test.setup.C1177100Setup;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

@Category(AirportQueue.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class, TestSetupConfig.class}, initializers = RAApplicationInitializer.class)
public class C1183001IT extends AbstractNonTxTests<C1177100Setup> {

  @Inject
  private DriverAction driverAction;
  @Inject
  private RiderAction riderAction;
  @Inject
  private AreaQueueUpdateService queueService;
  @Inject
  private EventAssertHelper eventAssertHelper;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    this.setup = createSetup();
  }

  @Test
  @TestCases("C1183001")
  public void test() throws Exception {
    Rider rider = setup.getRider();
    ActiveDriver activeDriver = setup.getActiveDriver();
    Driver driver = activeDriver.getDriver();
    String login = driver.getEmail();

    final LatLng pickup = locationProvider.getRandomLocation();
    driverAction.goOnline(login, pickup);

    final Long rideId = riderAction.requestRide(rider.getEmail(), pickup);

    awaitDispatch(activeDriver, rideId);

    LatLng airportLocation = locationProvider.getAirportLocation();
    new CascadedDriverAction(driver, driverAction)
      .withRideId(rideId)
      .acceptRide()
      .reach()
      .startRide()
      .endRide(airportLocation.lat, airportLocation.lng);

    awaitStatus(rideId, RideStatus.COMPLETED);

    driverAction
      .locationUpdate(activeDriver, airportLocation.lat, airportLocation.lng)
      .andExpect(status().isOk());

    Area airport = locationProvider.getAirport();
    queueService.updateStatuses(airport.getId());

    eventAssertHelper.assertLastEventIsSent(driver, EventType.QUEUED_AREA_ENTERING);

    AreaQueuePositions queueResponse = driverAction.getQueuePosition(driver);
    DriverQueueAssert.assertThat(queueResponse)
      .hasName(airport.getName())
      .hasPosition(TestUtils.REGULAR, 0)
      .hasLength(TestUtils.REGULAR, 1);
  }
}
