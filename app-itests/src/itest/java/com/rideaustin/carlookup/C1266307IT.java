package com.rideaustin.carlookup;

import static org.junit.Assert.assertEquals;

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
public class C1266307IT extends AbstractCarLookupTest {

  @Inject
  @Named("regularCar")
  private CarFixture carFixture;

  @Inject
  private ConfigurationItemCache configurationItemCache;

  private ActiveDriver woDriver;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    woDriver = activeDriverFixtureProvider.create(driverFixtureProvider.create(carFixture, 1)).getFixture();
    configurationItemCache.setIntConfig(Constants.DEFAULT_CITY_ID, ClientType.CONSOLE, "stackedRides", "dropoffExpectationTime", 60);
  }

  @Test
  @TestCases("C1266307")
  public void testSearchWOMode() throws Exception {
    LatLng center = locationProvider.getCenter();
    driverAction.locationUpdate(woDriver, center.lat, center.lng, new String[]{TestUtils.REGULAR}, new String[]{"WOMEN_ONLY"});

    List<CompactActiveDriverDto> drivers = riderAction.searchDrivers(rider.getEmail(), center, TestUtils.REGULAR, "WOMEN_ONLY");
    assertEquals(1, drivers.size());
    assertEquals(woDriver.getDriver().getUser().getId(), drivers.get(0).getDriver().getUser().getId());
  }

  @Test
  @TestCases("C1266307")
  public void testSearchRegularMode() throws Exception {
    LatLng center = locationProvider.getCenter();
    driverAction.locationUpdate(woDriver, center.lat, center.lng, new String[]{TestUtils.REGULAR}, new String[]{"WOMEN_ONLY"});

    List<CompactActiveDriverDto> drivers = riderAction.searchDrivers(rider.getEmail(), center, TestUtils.REGULAR, null);
    assertEquals(0, drivers.size());
  }
}
