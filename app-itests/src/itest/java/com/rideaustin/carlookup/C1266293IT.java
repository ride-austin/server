package com.rideaustin.carlookup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.maps.model.LatLng;
import com.rideaustin.Constants;
import com.rideaustin.dispatch.womenonly.WomenOnly;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.rest.model.CompactActiveDriverDto;
import com.rideaustin.test.fixtures.CarFixture;

import com.rideaustin.test.stubs.ConfigurationItemCache;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

@Category({CarLookup.class, WomenOnly.class})
public class C1266293IT extends AbstractCarLookupTest {

  @Inject
  @Named("suvCar")
  private CarFixture carFixture;

  @Inject
  private ConfigurationItemCache configurationItemCache;

  private List<ActiveDriver> drivers;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    drivers = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      ActiveDriver activeDriver;
      if (i % 2 == 0) {
        activeDriver = activeDriverFixtureProvider.create(driverFixtureProvider.create(carFixture, 1)).getFixture();
      } else {
        activeDriver = activeDriverFixtureProvider.create(driverFixtureProvider.create(carFixture)).getFixture();
      }
      drivers.add(activeDriver);
    }
    configurationItemCache.setIntConfig(Constants.DEFAULT_CITY_ID, ClientType.CONSOLE, "stackedRides", "dropoffExpectationTime", 60);
  }

  @Test
  @TestCases("C1266293")
  public void testSearchRegular() throws Exception {
    LatLng center = locationProvider.getCenter();
    double shift = 0.001;
    for (int i = 0; i < drivers.size(); i++) {
      if (i % 2 == 0) {
        driverAction.locationUpdate(drivers.get(i), center.lat + shift * i, center.lng + shift * i, new String[]{TestUtils.SUV}, new String[]{"WOMEN_ONLY"});
      } else {
        driverAction.locationUpdate(drivers.get(i), center.lat + shift * i, center.lng + shift * i, new String[]{TestUtils.SUV});
      }
    }

    List<CompactActiveDriverDto> foundDrivers = riderAction.searchDrivers(rider.getEmail(), center, TestUtils.SUV, null);
    assertFalse(foundDrivers.isEmpty());
    assertEquals(10L, (long) foundDrivers.get(0).getDrivingTimeToRider());
    assertEquals(drivers.get(1).getDriver().getUser().getId(), foundDrivers.get(0).getDriver().getUser().getId());
  }

  @Test
  @TestCases("C1266293")
  public void testSearchWomenOnly() throws Exception {
    LatLng center = locationProvider.getCenter();
    double shift = 0.001;
    for (int i = 0; i < drivers.size(); i++) {
      if (i % 2 == 0) {
        driverAction.locationUpdate(drivers.get(i), center.lat + shift * i, center.lng + shift * i, new String[]{TestUtils.SUV}, new String[]{"WOMEN_ONLY"});
      } else {
        driverAction.locationUpdate(drivers.get(i), center.lat + shift * i, center.lng + shift * i, new String[]{TestUtils.SUV});
      }
    }

    List<CompactActiveDriverDto> foundDrivers = riderAction.searchDrivers(rider.getEmail(), center, TestUtils.SUV, "WOMEN_ONLY");
    assertFalse(foundDrivers.isEmpty());
    assertEquals(0L, (long) foundDrivers.get(0).getDrivingTimeToRider());
    assertEquals(drivers.get(0).getDriver().getUser().getId(), foundDrivers.get(0).getDriver().getUser().getId());
  }
}
