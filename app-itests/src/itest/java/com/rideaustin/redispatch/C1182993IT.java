package com.rideaustin.redispatch;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.maps.model.LatLng;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.test.setup.DefaultRedispatchTestSetup;
import com.rideaustin.testrail.TestCases;

/**
 * https://testrail.devfactory.com/index.php?/cases/view/1182993
 */
@Category(Redispatch.class)
public class C1182993IT extends AbstractRedispatchTest<DefaultRedispatchTestSetup> {

  public static final String ASSERT_RATING_QUERY = "select count(*) from rating_updates where ride_id = ? and rated_avatar_id = ?";

  private LatLng destination;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    destination = locationProvider.getAirportLocation();
  }

  @Test
  @TestCases("C1182993")
  public void test() throws Exception {
    doTestRedispatch(destination);
  }

  @Override
  protected void assertRideRedispatched(LatLng destination, ActiveDriver secondDriver, LatLng riderLocation, LatLng secondDriverLocation, Long ride) throws Exception {
    driverAction.reach(secondDriver.getDriver().getEmail(), ride)
      .andExpect(status().isOk());
    driverAction.startRide(secondDriver.getDriver().getEmail(), ride)
      .andExpect(status().isOk());
    driverAction.endRide(secondDriver.getDriver().getEmail(), ride, destination.lat, destination.lng)
      .andExpect(status().isOk());

    double rating = 5.0;
    riderAction.rateRide(rider.getEmail(), ride, BigDecimal.valueOf(rating))
      .andExpect(status().isOk());

    Long firstDriverRatings = JDBC_TEMPLATE.queryForObject(ASSERT_RATING_QUERY, Long.class, ride, firstDriver.getDriver().getId());
    Long secondDriverRatings = JDBC_TEMPLATE.queryForObject(ASSERT_RATING_QUERY, Long.class, ride, secondDriver.getDriver().getId());

    assertEquals("First driver is expected not to receive any ratings", 0L, (long) firstDriverRatings);
    assertEquals("Second driver is expected to receive 1 rating of "+rating, 1L, (long) secondDriverRatings);
  }
}
