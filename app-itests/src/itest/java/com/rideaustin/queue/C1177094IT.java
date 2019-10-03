package com.rideaustin.queue;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.maps.model.LatLng;
import com.rideaustin.driverstatus.DriverStatusManagement;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Driver;
import com.rideaustin.test.asserts.DriverQueueAssert;
import com.rideaustin.test.response.AreaQueuePositions;
import com.rideaustin.test.setup.BaseRidesInQueueSetup;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

@Category({AirportQueue.class, DriverStatusManagement.class})
public class C1177094IT extends AbstractRidesInQueueTest<BaseRidesInQueueSetup> {

  @Test
  @TestCases("C1177094")
  public void test() throws Exception {
    ActiveDriver availableActiveDriver = setup.getAvailableActiveDriver();
    LatLng airportLocation = locationProvider.getAirportLocation();
    Driver driver = availableActiveDriver.getDriver();

    driverAction.goOnline(availableActiveDriver.getDriver().getEmail(), airportLocation)
      .andExpect(status().isOk());

    driverAction
      .locationUpdate(availableActiveDriver, airportLocation.lat, airportLocation.lng)
      .andExpect(status().isOk());
    updateQueue();

    AreaQueuePositions queuePosition = driverAction.getQueuePosition(driver);
    DriverQueueAssert.assertThat(queuePosition)
      .hasCategory(TestUtils.REGULAR);

    for (int i = 0; i < 2; i++) {
      requestAndDecline(availableActiveDriver, airportLocation);
    }

    updateQueue();

    queuePosition = driverAction.getQueuePosition(driver);
    DriverQueueAssert.assertThat(queuePosition)
      .hasNotCategory(TestUtils.REGULAR);
  }
}
