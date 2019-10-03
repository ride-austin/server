package com.rideaustin.carlookup;

import static org.junit.Assert.assertEquals;

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

@Category({WomenOnly.class, CarLookup.class})
public class C1266292IT extends AbstractCarLookupTest {

  @Inject
  @Named("premiumCar")
  private CarFixture carFixture;

  @Inject
  private ConfigurationItemCache configurationItemCache;

  private List<ActiveDriver> drivers;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    drivers = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      ActiveDriver activeDriver = activeDriverFixtureProvider.create(driverFixtureProvider.create(carFixture, 1)).getFixture();
      drivers.add(activeDriver);
    }
    for (int i = 0; i < 10; i++) {
      ActiveDriver activeDriver = activeDriverFixtureProvider.create(driverFixtureProvider.create(carFixture)).getFixture();
      drivers.add(activeDriver);
    }
    configurationItemCache.setIntConfig(Constants.DEFAULT_CITY_ID, ClientType.CONSOLE, "stackedRides", "dropoffExpectationTime", 60);
  }

  @Test
  @TestCases("C1266292")
  public void testSearchRegular() throws Exception {
    LatLng center = locationProvider.getCenter();
    double shift = 0.0001;
    for (int i = 0; i < drivers.size(); i++) {
      driverAction.locationUpdate(drivers.get(i), center.lat + shift * i, center.lng + shift * i, new String[]{TestUtils.PREMIUM});
    }

    List<CompactActiveDriverDto> foundDrivers = riderAction.searchDrivers(rider.getEmail(), center, TestUtils.PREMIUM, null);
    assertEquals(10, foundDrivers.size());
    assertEquals(0L, (long) foundDrivers.get(0).getDrivingTimeToRider());
  }

  @Test
  @TestCases("C1266292")
  public void testSearchWomenOnly() throws Exception {
    LatLng center = locationProvider.getCenter();
    double shift = 0.0001;
    for (int i = 0; i < drivers.size(); i++) {
      if (i < 5) {
        driverAction.locationUpdate(drivers.get(i), center.lat + shift * i, center.lng + shift * i, new String[]{TestUtils.PREMIUM}, new String[]{"WOMEN_ONLY"});
      } else {
        driverAction.locationUpdate(drivers.get(i), center.lat + shift * i, center.lng + shift * i, new String[]{TestUtils.PREMIUM});
      }
    }

    List<CompactActiveDriverDto> foundDrivers = riderAction.searchDrivers(rider.getEmail(), center, TestUtils.PREMIUM, "WOMEN_ONLY");
    assertEquals(5, foundDrivers.size());
    assertEquals(0L, (long) foundDrivers.get(0).getDrivingTimeToRider());
    assertEquals(drivers.get(0).getDriver().getUser().getId(), foundDrivers.get(0).getDriver().getUser().getId());
  }
}
