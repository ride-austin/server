package com.rideaustin.queue;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.google.maps.model.LatLng;
import com.rideaustin.Constants;
import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.Area;
import com.rideaustin.model.enums.EventType;
import com.rideaustin.model.user.Driver;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.areaqueue.AreaQueueUpdateService;
import com.rideaustin.test.LocationProvider;
import com.rideaustin.test.actions.DriverAction;
import com.rideaustin.test.asserts.DriverQueueAssert;
import com.rideaustin.test.asserts.EventAssertHelper;
import com.rideaustin.test.common.ITestProfileSupport;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.response.AreaQueuePositions;
import com.rideaustin.test.stubs.ConfigurationItemCache;
import com.rideaustin.test.util.TestUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public abstract class AbstractAirportQueueTest extends ITestProfileSupport {

  @Inject
  protected AreaQueueUpdateService queueService;

  @Inject
  private EventAssertHelper eventAssertHelper;

  protected LatLng airportLocation;
  protected LatLng outsideAirportLocation;
  protected Area airport;

  @Inject
  protected DriverAction driverAction;

  @Inject
  protected LocationProvider locationProvider;
  @Inject
  private ConfigurationItemCache configurationItemCache;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    airport = locationProvider.getAirport();
    airportLocation = locationProvider.getAirportLocation();
    outsideAirportLocation = locationProvider.getOutsideAirportLocation();
    configurationItemCache.setBooleanConfig(Constants.DEFAULT_CITY_ID, ClientType.CONSOLE, "driverStats", "enabled", false);
    configurationItemCache.setBooleanConfig(Constants.DEFAULT_CITY_ID, ClientType.CONSOLE, "queue", "penaltyEnabled", false);
  }

  protected void assertQueuePosition(Driver driver) throws Exception {
    AreaQueuePositions queueResponse = driverAction.getQueuePosition(driver);
    DriverQueueAssert.assertThat(queueResponse)
      .hasName(airport.getName())
      .hasPosition(TestUtils.REGULAR, 0)
      .hasLength(TestUtils.REGULAR, 1);
  }

  protected void assertEventIsSent(Driver driver, EventType eventType) {
    eventAssertHelper.assertLastEventIsSent(driver, eventType);
  }

  protected void updateQueue() throws RideAustinException {
    queueService.updateStatuses(airport.getId());
  }
}
