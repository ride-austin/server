package com.rideaustin.queue;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.maps.model.LatLng;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.response.AreaQueuePositions;
import com.rideaustin.test.asserts.DriverQueueAssert;
import com.rideaustin.test.setup.BaseRidesInQueueSetup;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

@Category(AirportQueue.class)
public class C1183006IT extends AbstractRidesInQueueTest<BaseRidesInQueueSetup> {

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    this.setup = createSetup();
  }

  @Test
  @TestCases("C1183006")
  public void test() throws Exception {
    ActiveDriver availableActiveDriver = setup.getAvailableActiveDriver();
    LatLng airportLocation = locationProvider.getAirportLocation();
    Rider rider = setup.getRider();

    driverAction
      .locationUpdate(availableActiveDriver, airportLocation.lat, airportLocation.lng)
      .andExpect(status().isOk());
    updateQueue();

    AreaQueuePositions queuePosition = driverAction.getQueuePosition(availableActiveDriver.getDriver());
    DriverQueueAssert.assertThat(queuePosition)
      .hasLength(TestUtils.REGULAR, 1)
      .hasPosition(TestUtils.REGULAR, 0);

    Long ride = riderAction.requestRide(rider.getEmail(), airportLocation);

    awaitDispatch(availableActiveDriver, ride);

    updateQueue();

    driverAction
      .acceptRide(availableActiveDriver, ride)
      .andExpect(status().isOk());

    riderAction
      .cancelRide(rider.getEmail(), ride)
      .andExpect(status().isOk());

    updateQueue();

    queuePosition = driverAction.getQueuePosition(availableActiveDriver.getDriver());
    DriverQueueAssert.assertThat(queuePosition)
      .hasLength(TestUtils.REGULAR, 1)
      .hasPosition(TestUtils.REGULAR, 0);
  }
}
