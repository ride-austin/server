package com.rideaustin.redispatch;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.maps.model.LatLng;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.test.setup.DefaultRedispatchTestSetup;
import com.rideaustin.testrail.TestCases;

/**
 * https://testrail.devfactory.com/index.php?/cases/view/1182971
 */
@Category(Redispatch.class)
public class C1182971IT extends AbstractRedispatchTest<DefaultRedispatchTestSetup> {

  private LatLng newDestination;

  @Test
  @TestCases("C1182971")
  public void test() throws Exception {
    doTestRedispatch(locationProvider.getAirportLocation());
  }

  @Override
  protected Long requestAndAccept(LatLng destination, ActiveDriver firstDriver, LatLng riderLocation) throws Exception {
    Long rideId = super.requestAndAccept(destination, firstDriver, riderLocation);
    newDestination = new LatLng(destination.lat+0.01, destination.lng+0.01);
    riderAction.updateRideDestination(rider.getEmail(), rideId, newDestination)
      .andExpect(status().isOk());
    return rideId;
  }

  @Override
  protected void assertRideRedispatched(LatLng destination, ActiveDriver secondDriver, LatLng riderLocation, LatLng secondDriverLocation, Long ride) throws Exception {
    super.assertRideRedispatched(newDestination, secondDriver, riderLocation, secondDriverLocation, ride);
  }
}
