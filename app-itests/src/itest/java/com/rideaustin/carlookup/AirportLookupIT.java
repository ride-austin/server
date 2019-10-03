package com.rideaustin.carlookup;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;

import com.google.maps.model.LatLng;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.enums.CityApprovalStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.DriverType;
import com.rideaustin.rest.model.CompactActiveDriverDto;
import com.rideaustin.service.areaqueue.AreaQueueUpdateService;
import com.rideaustin.test.fixtures.CarFixture;
import com.rideaustin.test.stubs.ConfigurationItemCache;
import com.rideaustin.test.util.TestUtils;

public class AirportLookupIT extends AbstractCarLookupTest {

  private ActiveDriver driverFP;
  private ActiveDriver driverFPWO;
  private ActiveDriver driverFPWOO;
  private ActiveDriver driverFPDCO;
  private ActiveDriver driver;
  private ActiveDriver driverWO;
  private ActiveDriver driverWOO;
  private ActiveDriver driverDCO;

  @Inject
  private CarFixture carFixture;
  @Inject
  private AreaQueueUpdateService queueService;
  @Inject
  private ConfigurationItemCache configurationItemCache;

  @Before
  public void setUp() throws Exception {
    super.setUp();

    driver = activeDriverFixtureProvider.create(driverFixtureProvider.create(carFixture, 0, CityApprovalStatus.PENDING)).getFixture();
    driverWO = activeDriverFixtureProvider.create(driverFixtureProvider.create(carFixture, 1, CityApprovalStatus.PENDING)).getFixture();
    driverWOO = activeDriverFixtureProvider.create(driverFixtureProvider.create(carFixture, 1, CityApprovalStatus.PENDING)).getFixture();
    driverDCO = activeDriverFixtureProvider.create(driverFixtureProvider.create(carFixture, 2, CityApprovalStatus.PENDING)).getFixture();
    driverFP = activeDriverFixtureProvider.create(driverFixtureProvider.create(carFixture, 4)).getFixture();
    driverFPWO = activeDriverFixtureProvider.create(driverFixtureProvider.create(carFixture, 5)).getFixture();
    driverFPWOO = activeDriverFixtureProvider.create(driverFixtureProvider.create(carFixture, 5)).getFixture();
    driverFPDCO = activeDriverFixtureProvider.create(driverFixtureProvider.create(carFixture, 6)).getFixture();

    configurationItemCache.setBooleanConfig(null, ClientType.CONSOLE, "queue", "penaltyEnabled", false);
  }

  @Test
  public void test1() throws Exception {
    goOnline();
    queueService.updateStatuses(locationProvider.getAirport().getId());

    final List<CompactActiveDriverDto> result = riderAction.searchDrivers(rider.getEmail(), locationProvider.getAirportLocation(), TestUtils.REGULAR, null);
    assertEquals(4, result.size());
    assertThat(result, containsInAnyOrder(Arrays.asList(
      new DriverMatcher(driver.getDriver().getUser().getId()),
      new DriverMatcher(driverFP.getDriver().getUser().getId()),
      new DriverMatcher(driverWO.getDriver().getUser().getId()),
      new DriverMatcher(driverFPWO.getDriver().getUser().getId())
    )));
  }

  @Test
  public void test2() throws Exception {
    goOnline();
    queueService.updateStatuses(locationProvider.getAirport().getId());

    final List<CompactActiveDriverDto> result = riderAction.searchDrivers(rider.getEmail(), locationProvider.getAirportLocation(), TestUtils.REGULAR, DriverType.FINGERPRINTED);
    assertEquals(2, result.size());
    assertThat(result, containsInAnyOrder(Arrays.asList(
      new DriverMatcher(driverFP.getDriver().getUser().getId()),
      new DriverMatcher(driverFPWO.getDriver().getUser().getId())
    )));
  }

  protected void goOnline() throws Exception {
    final LatLng airport = locationProvider.getAirportLocation();
    driverAction.goOnline(driver.getDriver().getEmail(), airport).andExpect(status().isOk());
    driverAction.locationUpdate(driver, airport.lat, airport.lng).andExpect(status().isOk());
    driverAction.goOnline(driverWO.getDriver().getEmail(), airport).andExpect(status().isOk());
    driverAction.locationUpdate(driverWO, airport.lat, airport.lng).andExpect(status().isOk());
    driverAction.goOnline(driverWOO.getDriver().getEmail(), airport).andExpect(status().isOk());
    driverAction.locationUpdate(driverWOO, airport.lat, airport.lng, new String[]{TestUtils.REGULAR}, new String[]{DriverType.WOMEN_ONLY}).andExpect(status().isOk());
    driverAction.goOnline(driverDCO.getDriver().getEmail(), airport).andExpect(status().isOk());
    driverAction.locationUpdate(driverDCO, airport.lat, airport.lng, new String[]{TestUtils.REGULAR}, new String[]{DriverType.DIRECT_CONNECT}).andExpect(status().isOk());
    driverAction.goOnline(driverFP.getDriver().getEmail(), airport).andExpect(status().isOk());
    driverAction.locationUpdate(driverFP, airport.lat, airport.lng).andExpect(status().isOk());
    driverAction.goOnline(driverFPWO.getDriver().getEmail(), airport).andExpect(status().isOk());
    driverAction.locationUpdate(driverFPWO, airport.lat, airport.lng).andExpect(status().isOk());
    driverAction.goOnline(driverFPWOO.getDriver().getEmail(), airport).andExpect(status().isOk());
    driverAction.locationUpdate(driverFPWOO, airport.lat, airport.lng, new String[]{TestUtils.REGULAR}, new String[]{DriverType.WOMEN_ONLY}).andExpect(status().isOk());
    driverAction.goOnline(driverFPDCO.getDriver().getEmail(), airport).andExpect(status().isOk());
    driverAction.locationUpdate(driverFPDCO, airport.lat, airport.lng, new String[]{TestUtils.REGULAR}, new String[]{DriverType.DIRECT_CONNECT}).andExpect(status().isOk());
  }

  private static class DriverMatcher extends BaseMatcher<CompactActiveDriverDto> {

    private final long userId;

    DriverMatcher(long userId) {
      this.userId = userId;
    }

    @Override
    public boolean matches(Object o) {
      return ((CompactActiveDriverDto) o).getDriver().getUser().getId() == userId;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(String.valueOf(userId));
    }
  }
}
