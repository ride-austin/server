package com.rideaustin.redispatch;

import static org.junit.Assert.assertFalse;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.LinkedList;

import javax.inject.Inject;

import org.junit.Before;
import org.springframework.test.context.ContextConfiguration;

import com.google.maps.model.LatLng;
import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.rest.model.MobileRiderRideDto;
import com.rideaustin.service.MapService;
import com.rideaustin.service.config.RideDispatchServiceConfig;
import com.rideaustin.service.model.States;
import com.rideaustin.test.AbstractNonTxTests;
import com.rideaustin.test.actions.DriverAction;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.asserts.RiderRideAssert;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.config.TestSetupConfig;
import com.rideaustin.test.setup.BaseRedispatchTestSetup;
import com.rideaustin.test.stubs.ConfigurationItemCache;
import com.rideaustin.test.stubs.NotificationFacade;
import com.rideaustin.test.util.TestUtils;

@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class, TestSetupConfig.class}, initializers = RAApplicationInitializer.class)
public abstract class AbstractRedispatchTest<T extends BaseRedispatchTestSetup<T>> extends AbstractNonTxTests<T> {

  @Inject
  private ConfigurationItemCache cache;

  @Inject
  protected DriverAction driverAction;

  @Inject
  protected RiderAction riderAction;

  @Inject
  private RideDispatchServiceConfig rideDispatchServiceConfig;

  @Inject
  protected NotificationFacade notificationFacade;

  @Inject
  private MapService mapService;

  protected long dispatchTimeout;
  private long expectationTimeout;

  protected Rider rider;
  protected ActiveDriver firstDriver;
  protected ActiveDriver secondDriver;

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
    cache.setBooleanConfig(1L, ClientType.CONSOLE, "redispatchOnCancel", "enabled", true);
    cache.setBooleanConfig(1L, ClientType.CONSOLE, "rideMessaging", "enabled", false);

    dispatchTimeout = rideDispatchServiceConfig.getPerDriverWaitTime(1L);
    expectationTimeout = environment.getProperty("dispatch.addSecondsToDrivingTime", Long.class, 0L);

    this.setup = createSetup();
    rider = this.setup.getRider();
    firstDriver = this.setup.getFirstActiveDriver();
    secondDriver = this.setup.getSecondActiveDriver();
  }

  protected void doTestRedispatch(LatLng destination) throws Exception {
    LatLng riderLocation = locationProvider.getCenter();
    LatLng firstDriverLocation = riderLocation;
    LatLng secondDriverLocation = new LatLng(riderLocation.lat + 0.01, riderLocation.lng + 0.01);
    goOnline(firstDriver, secondDriver, firstDriverLocation, secondDriverLocation);

    Long ride = requestAndAccept(destination, firstDriver, riderLocation);

    assertRideAssigned(firstDriver, riderLocation, firstDriverLocation, ride);

    cancelAndRedispatch(firstDriver, secondDriver, ride);

    assertRideRedispatched(destination, secondDriver, riderLocation, secondDriverLocation, ride);
  }

  protected void assertRideRedispatched(LatLng destination, ActiveDriver secondDriver, LatLng riderLocation, LatLng secondDriverLocation, Long ride) throws Exception {
    MobileRiderRideDto rideInfo = riderAction.getRideInfo(rider.getEmail(), ride);

    RiderRideAssert.assertThat(rideInfo)
      .hasRequestedCategory(TestUtils.REGULAR)
      .hasDriverAssigned(secondDriver.getId())
      .hasETA(Math.max(mapService.computeDistanceTime(riderLocation, secondDriverLocation).getTime(), expectationTimeout))
      .hasStartLocation(riderLocation)
      .hasEndLocation(destination);
  }

  protected void cancelAndRedispatch(ActiveDriver firstDriver, ActiveDriver secondDriver, Long ride) throws Exception {
    driverAction.cancelRide(firstDriver.getDriver().getEmail(), ride)
      .andExpect(status().isOk());

    awaitState(ride, States.DRIVER_CANCELLED, States.REQUESTED, States.DISPATCH_PENDING);

    //assert that rider received no notifications
    assertFalse("Rider should not be notified about cancellation", notificationFacade.getDataMap().keySet().contains(RideStatus.DRIVER_CANCELLED.name()));

    acceptRedispatched(secondDriver, ride);
  }

  protected void acceptRedispatched(ActiveDriver driver, Long ride) throws Exception {
    acceptRide(driver, ride);
  }

  protected void acceptRide(ActiveDriver driver, Long ride) throws Exception {
    awaitDispatch(driver, ride, dispatchTimeout, environment, contextAccess);

    driverAction.acceptRide(driver, ride)
      .andExpect(status().isOk());

    awaitState(ride, States.DRIVER_ASSIGNED);
    awaitStatus(ride, RideStatus.DRIVER_ASSIGNED);
  }

  protected MobileRiderRideDto assertRideAssigned(ActiveDriver driver, LatLng riderLocation, LatLng driverLocation, Long ride) throws Exception {
    MobileRiderRideDto rideInfo = riderAction.getRideInfo(rider.getEmail(), ride);
    RiderRideAssert.assertThat(rideInfo)
      .hasDriverAssigned(driver.getId())
      .hasETA(mapService.computeDistanceTime(riderLocation, driverLocation).getTime());
    return rideInfo;
  }

  protected Long requestAndAccept(LatLng destination, ActiveDriver firstDriver, LatLng riderLocation) throws Exception {
    Long ride = requestRide(destination, riderLocation);

    acceptRide(firstDriver, ride);
    return ride;
  }

  protected Long requestRide(LatLng destination, LatLng riderLocation) throws Exception {
    return riderAction.requestRide(rider.getEmail(), riderLocation, destination);
  }

  protected void goOnline(ActiveDriver firstDriver, ActiveDriver secondDriver, LatLng firstDriverLocation, LatLng secondDriverLocation) throws Exception {
    driverAction.locationUpdate(firstDriver, firstDriverLocation.lat, firstDriverLocation.lng)
      .andExpect(status().isOk());

    driverAction.locationUpdate(secondDriver, secondDriverLocation.lat, secondDriverLocation.lng)
      .andExpect(status().isOk());
  }

  protected LinkedList<Long> getDispatchHistory(Long ride) {
    return new LinkedList<>(
      JDBC_TEMPLATE.query("select active_driver_id from ride_driver_dispatches where ride_id = ? order by id",
        new Object[]{ride}, (rs, rowNum) -> rs.getLong(1))
    );
  }

}
