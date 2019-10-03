package com.rideaustin.queue;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.maps.model.LatLng;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.test.response.AreaQueuePositions;
import com.rideaustin.test.asserts.DriverQueueAssert;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

@Category(AirportQueue.class)
public class C1183009IT extends AbstractDispatchedRideInQueueTest {

  @Test
  @TestCases("C1183009")
  public void test() throws Exception {
    ActiveDriver driver1 = setup.getFirstDriver();
    ActiveDriver driver2 = setup.getSecondDriver();

    LatLng airportLocation = locationProvider.getAirportLocation();
    driverAction
      .locationUpdate(driver1, airportLocation.lat, airportLocation.lng)
      .andExpect(status().isOk());
    updateQueue();

    sleeper.sleep(500);

    driverAction
      .locationUpdate(driver2, airportLocation.lat, airportLocation.lng)
      .andExpect(status().isOk());
    updateQueue();

    assertPosition(driver1, 0, 2);
    assertPosition(driver2, 1, 2);

    Long ride = riderAction.requestRide(setup.getRider().getEmail(), airportLocation);

    acceptAndStartRide(driver1, ride);

    assertPosition(driver2, 0, 1);
  }

  private void assertPosition(ActiveDriver activeDriver, int position, int length) throws Exception {
    AreaQueuePositions queuePosition1 = driverAction.getQueuePosition(activeDriver.getDriver());
    DriverQueueAssert.assertThat(queuePosition1)
      .hasPosition(TestUtils.REGULAR, position)
      .hasLength(TestUtils.REGULAR, length);
  }

}
