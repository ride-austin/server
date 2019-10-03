package com.rideaustin.activedrivers;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.maps.model.LatLng;
import com.rideaustin.Constants;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;

import com.rideaustin.test.stubs.ConfigurationItemCache;
import com.rideaustin.test.util.TestUtils;

@Category(ActiveDriver.class)
public class RA11738DispatchToLuxuryProblemFixIT extends AbstractActiveDriverTest {

  private LatLng defaultClosestLocation = new LatLng(30.269372, -97.740394);
  private LatLng defaultNotClosestLocation = new LatLng(30.263372, -97.744394);

  @Inject
  private ConfigurationItemCache configurationItemCache;

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
    configurationItemCache.setIntConfig(Constants.DEFAULT_CITY_ID, ClientType.CONSOLE, "stackedRides", "dropoffExpectationTime", 60);
  }

  @Test
  public void test() throws Exception {

    Rider rider = riderfixture.getFixture();

    List<ActiveDriver> randomActiveDrivers = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      randomActiveDrivers.add(activeDriverFixtureProvider.create(driverFixtureProvider.create(luxuryCarFixture)).getFixture());
    }
    logger.info("Active drivers created");

    assertNumberOfActiveDriver(20);
    for (ActiveDriver randomActiveDriver : randomActiveDrivers) {
      driverAction.locationUpdate(randomActiveDriver, defaultClosestLocation.lat, defaultClosestLocation.lng);
    }
    driverAction.locationUpdate(randomActiveDrivers.get(0), defaultNotClosestLocation.lat, defaultNotClosestLocation.lng, new String[]{"HONDA"});

    String content = riderAction.getClosestActiveDriver(rider.getEmail(), defaultClosestLocation, TestUtils.HONDA);
    assertCorrectDriver(content, randomActiveDrivers.get(0));

  }
}



