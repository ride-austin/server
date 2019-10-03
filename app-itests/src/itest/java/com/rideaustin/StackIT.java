package com.rideaustin;

import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Ignore;
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
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.AbstractNonTxTests;
import com.rideaustin.test.actions.DriverAction;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.setup.StackSetup;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public class StackIT extends AbstractNonTxTests<StackSetup> {

  private List<ActiveDriver> activeDrivers;
  private List<Rider> riders;

  @Inject
  private DriverAction driverAction;
  @Inject
  private RiderAction riderAction;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    setup = createSetup();
    activeDrivers = setup.getActiveDrivers();
    riders = setup.getRiders();
    configurationItemCache.setBooleanConfig(1L, ClientType.CONSOLE, "stackedRides", "enabled", true);
  }

  @Test
  public void test() throws Exception {
    LatLng center = locationProvider.getCenter();
    for (int i = 0; i < 5; i++) {
      ActiveDriver activeDriver = activeDrivers.get(i);
      String email = activeDriver.getDriver().getEmail();
      driverAction.goOnline(email, center);
      Long ride = riderAction.requestRide(riders.get(i).getEmail(), center, center);

      awaitDispatch(activeDriver, ride);
      driverAction.acceptRide(email, ride);
      driverAction.reach(email, ride);
      driverAction.startRide(email, ride);
    }

    for (int i = 0; i < 5; i++) {
      ActiveDriver activeDriver = activeDrivers.get(i);
      driverAction.locationUpdate(activeDriver, center.lat, center.lng);
    }

    Rider rider = riders.get(5);
    Long ride = riderAction.requestRide(rider.getEmail(), center, center);

    awaitDispatch(activeDrivers.get(4), ride);
  }
}
