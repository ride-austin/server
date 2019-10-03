package com.rideaustin.queue;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.maps.model.LatLng;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Driver;
import com.rideaustin.test.response.AreaQueuePositions;
import com.rideaustin.test.asserts.DriverQueueAssert;

import com.rideaustin.test.setup.BaseRidesInQueueSetup;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

@Category(AirportQueue.class)
public class C1183007IT extends AbstractRidesInQueueTest<BaseRidesInQueueSetup> {

  @Test
  @TestCases("C1183007")
  public void test() throws Exception {
    ActiveDriver availableActiveDriver = setup.getAvailableActiveDriver();
    LatLng airportLocation = locationProvider.getAirportLocation();
    LatLng outsideAirportLocation = locationProvider.getOutsideAirportLocation();
    Driver driver = availableActiveDriver.getDriver();

    driverAction
      .locationUpdate(availableActiveDriver, airportLocation.lat, airportLocation.lng, new String[]{TestUtils.REGULAR, TestUtils.SUV})
      .andExpect(status().isOk());
    updateQueue();

    AreaQueuePositions queuePosition = driverAction.getQueuePosition(driver);
    DriverQueueAssert.assertThat(queuePosition)
      .hasCategory(TestUtils.SUV)
      .hasCategory(TestUtils.REGULAR);

    for (int i = 0; i < 2; i++) {
      requestAndDecline(availableActiveDriver, outsideAirportLocation);
    }

    updateQueue();

    queuePosition = driverAction.getQueuePosition(driver);
    DriverQueueAssert.assertThat(queuePosition)
      .hasCategory(TestUtils.SUV)
      .hasCategory(TestUtils.REGULAR);
  }
}
