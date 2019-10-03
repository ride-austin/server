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

@Category({WomenOnly.class, CarLookup.class})
public class C1266313IT extends AbstractCarLookupTest {

  @Inject
  @Named("allTypesCar")
  private CarFixture carFixture;

  @Inject
  private ConfigurationItemCache configurationItemCache;

  private ActiveDriver woDriver;
  private ActiveDriver regularDriver;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    woDriver = activeDriverFixtureProvider.create(driverFixtureProvider.create(carFixture, 1)).getFixture();
    regularDriver = activeDriverFixtureProvider.create(driverFixtureProvider.create(carFixture)).getFixture();
    configurationItemCache.setIntConfig(Constants.DEFAULT_CITY_ID, ClientType.CONSOLE, "stackedRides", "dropoffExpectationTime", 60);
  }

  @Test
  @TestCases("C1266313")
  public void testSearchRegular() throws Exception {
    doTest(TestUtils.REGULAR);
  }

  @Test
  @TestCases("C1266313")
  public void testSearchSUV() throws Exception {
    doTest(TestUtils.SUV);
  }

  @Test
  @TestCases("C1266313")
  public void testSearchPremium() throws Exception {
    doTest(TestUtils.PREMIUM);
  }

  private void doTest(String carCategory) throws Exception {
    LatLng center = locationProvider.getCenter();
    driverAction.locationUpdate(woDriver, center.lat, center.lng, new String[]{TestUtils.REGULAR, TestUtils.SUV, TestUtils.PREMIUM}, new String[]{"WOMEN_ONLY"});
    driverAction.locationUpdate(regularDriver, center.lat, center.lng, new String[]{TestUtils.REGULAR, TestUtils.SUV, TestUtils.PREMIUM});

    List<CompactActiveDriverDto> drivers = riderAction.searchDrivers(rider.getEmail(), center, carCategory, "WOMEN_ONLY");

    assertEquals(1, drivers.size());
    assertEquals(woDriver.getDriver().getUser().getId(), drivers.get(0).getDriver().getUser().getId());
  }
}
