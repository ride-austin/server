package com.rideaustin.dispatch.womenonly;

import static org.junit.Assert.assertTrue;

import java.util.List;

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
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.DriverType;
import com.rideaustin.model.user.Rider;
import com.rideaustin.rest.model.CompactActiveDriverDto;
import com.rideaustin.test.AbstractNonTxTests;
import com.rideaustin.test.actions.DriverAction;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.config.TestSetupConfig;
import com.rideaustin.test.setup.RA142DCOnlyFPRiderSetup;
import com.rideaustin.test.util.TestUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class, TestSetupConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public class RA142DCOnlyFPRiderIT extends AbstractNonTxTests<RA142DCOnlyFPRiderSetup> {

  @Inject
  private RiderAction riderAction;
  @Inject
  private DriverAction driverAction;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    setup = createSetup();
  }

  @Test
  public void test() throws Exception {
    final ActiveDriver driver = setup.getDriver();
    final LatLng location = locationProvider.getCenter();
    driverAction.goOnline(driver.getDriver().getEmail(), location);
    driverAction.locationUpdate(driver, location.lat, location.lng, new String[]{TestUtils.REGULAR}, new String[]{DriverType.DIRECT_CONNECT});

    final Rider rider = setup.getRider();
    final List<CompactActiveDriverDto> result = riderAction.searchDrivers(rider.getEmail(), location, TestUtils.REGULAR, DriverType.FINGERPRINTED);
    assertTrue(result.isEmpty());
  }
}
