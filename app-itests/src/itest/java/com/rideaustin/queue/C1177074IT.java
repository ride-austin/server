package com.rideaustin.queue;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.maps.model.LatLng;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.redispatch.Redispatch;
import com.rideaustin.test.setup.C1177074Setup;
import com.rideaustin.testrail.TestCases;

@Category({AirportQueue.class, Redispatch.class})
public class C1177074IT extends AbstractRidesInQueueTest<C1177074Setup> {

  private C1177074Setup setup;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    this.setup = createSetup();
  }

  @Test
  @TestCases("C1177074")
  public void test() throws Exception {

    ActiveDriver driver1 = setup.getDriver1();
    ActiveDriver driver2 = setup.getDriver2();
    ActiveDriver driver3 = setup.getDriver3();

    LatLng airportLocation = locationProvider.getAirportLocation();
    LatLng outsideAirportLocation = locationProvider.getOutsideAirportLocation();

    /**
     * First 2 drivers go online - 1st inside airport, 2nd outside airport
     */
    driverAction
      .locationUpdate(driver1, airportLocation.lat, airportLocation.lng)
      .andExpect(status().isOk());
    updateQueue();
    driverAction
      .locationUpdate(driver3, outsideAirportLocation.lat, outsideAirportLocation.lng)
      .andExpect(status().isOk());

    sleeper.sleep(500);

    /**
     * Second airport driver goes online
     */
    driverAction
      .locationUpdate(driver2, airportLocation.lat, airportLocation.lng)
      .andExpect(status().isOk());
    updateQueue();

    Rider rider = setup.getRider();

    /**
     * Ride is requested
     */
    Long rideId = riderAction.requestRide(rider.getEmail(), airportLocation);

    /**
     * Wait for ride to be dispatched to 1st driver and decline it
     */
    awaitDispatch(driver1, rideId);
    driverAction.declineRide(driver1, rideId);
    sleeper.sleep(500);
    /**
     * Ride should be redispatched to 2nd queue driver. Wait until driver misses the ride and becomes available
     */
    awaitDispatch(driver2, rideId);
    sleeper.sleep(13000);

    awaitDispatch(driver3, rideId);
  }
}
