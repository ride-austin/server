package com.rideaustin.cachedevents;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.google.maps.model.LatLng;
import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.AbstractNonTxTests;
import com.rideaustin.test.actions.DriverAction;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.actions.cascaded.CascadedDriverAction;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.ITestProfile;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.model.CachedEventFactory;
import com.rideaustin.test.setup.RA14466Setup;

@ITestProfile
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public class RA14466IT extends AbstractNonTxTests<RA14466Setup> {

  @Inject
  private RiderAction riderAction;
  @Inject
  private DriverAction driverAction;
  @Inject
  private CachedEventFactory eventFactory;

  private Rider firstRider;
  private Rider secondRider;
  private ActiveDriver driver;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    setup = createSetup();
    driver = setup.getDriver();
    firstRider = setup.getFirstRider();
    secondRider = setup.getSecondRider();
    configurationItemCache.setBooleanConfig(1L, ClientType.CONSOLE, "stackedRides", "enabled", true);
  }

  @Test
  public void test() throws Exception{
    LatLng center = locationProvider.getCenter();
    driverAction.goOnline(driver.getDriver().getEmail(), center)
      .andExpect(status().isOk());

    driverAction.locationUpdate(driver, center.lat, center.lng);
    Long regularRide = riderAction.requestRide(firstRider.getEmail(), center, center);

    CascadedDriverAction cascadedDriverAction = new CascadedDriverAction(driver.getDriver(), driverAction)
      .withRideId(regularRide);

    awaitDispatch(driver, regularRide);

    cascadedDriverAction
      .acceptRide()
      .reach()
      .startRide();

    driverAction.locationUpdate(driver, center.lat, center.lng);

    Long stackedRide = riderAction.requestRide(secondRider.getEmail(), center);

    awaitDispatch(driver, stackedRide);

    driverAction.acceptRide(driver, stackedRide)
      .andExpect(status().isOk());

    eventFactory.initializeTimestamp();
    driverAction.sendCached(driver.getDriver().getEmail(),
      eventFactory.createEndRideEvent(regularRide, center),
      eventFactory.createReachEvent(stackedRide))
      .andExpect(status().isOk());

    awaitStatus(regularRide, RideStatus.COMPLETED);
    awaitStatus(stackedRide, RideStatus.DRIVER_REACHED);
  }
}
