package com.rideaustin.driverstatus;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.google.maps.model.LatLng;
import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.model.Area;
import com.rideaustin.repo.dsl.DriverDslRepository;
import com.rideaustin.repo.dsl.SessionDslRepository;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.areaqueue.AreaQueueUpdateService;
import com.rideaustin.service.config.RideDispatchServiceConfig;
import com.rideaustin.test.actions.DriverAction;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.common.ITestProfileSupport;
import com.rideaustin.test.common.Sleeper;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.fixtures.CarFixture;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;
import com.rideaustin.test.fixtures.providers.DriverFixtureProvider;
import com.rideaustin.test.fixtures.providers.RiderFixtureProvider;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public abstract class AbstractDriverStatusManagementTest extends ITestProfileSupport {

  @Inject
  protected DriverAction driverAction;

  @Inject
  protected RiderAction riderAction;

  @Inject
  protected RiderFixtureProvider riderFixtureProvider;

  @Inject
  protected DriverFixtureProvider driverFixtureProvider;

  @Inject
  protected ActiveDriverFixtureProvider activeDriverFixtureProvider;

  @Inject
  protected CarFixture regularCar;

  @Inject
  protected RideDispatchServiceConfig rideDispatchServiceConfig;

  @Inject
  protected AreaQueueUpdateService queueService;

  @Inject
  protected SessionDslRepository sessionDslRepository;

  @Inject
  protected DriverDslRepository driverDslRepository;

  @Inject
  protected Sleeper sleeper;

  protected LatLng austinCenter;

  protected LatLng airportLocation;

  protected LatLng outsideAirportLocation;

  protected Area airport;

  protected int declinedRidesLimit;

  @Before
  public void driverStatusSetUp() throws Exception {
    super.setUp();
    airport = locationProvider.getAirport();
    austinCenter = locationProvider.getCenter();
    airportLocation = locationProvider.getAirportLocation();
    outsideAirportLocation = locationProvider.getOutsideAirportLocation();
    declinedRidesLimit = rideDispatchServiceConfig.getDriverMaxDeclinedRequests();
  }

  protected void updateQueue() throws RideAustinException {
    queueService.updateStatuses(airport.getId());
  }
}
