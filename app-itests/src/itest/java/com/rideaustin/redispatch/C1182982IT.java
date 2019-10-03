package com.rideaustin.redispatch;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.maps.model.LatLng;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.rest.model.MobileRiderRideDto;
import com.rideaustin.service.model.States;
import com.rideaustin.test.asserts.RiderRideAssert;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

/**
 * https://testrail.devfactory.com/index.php?/cases/view/1182982
 */
@Category(Redispatch.class)
public class C1182982IT extends AbstractRiderCancelRedispatchTest {

  @Test
  @TestCases("C1182982")
  public void test() throws Exception {
    doTestRedispatch(null);
  }

  @Override
  protected void acceptRedispatched(ActiveDriver driver, Long ride) throws Exception {
    riderAction.cancelRide(rider.getEmail(), ride);

    awaitState(ride, States.RIDER_CANCELLED, States.ENDED);
    awaitStatus(ride, RideStatus.RIDER_CANCELLED);
  }

  @Override
  protected void assertRideRedispatched(LatLng destination, ActiveDriver secondDriver, LatLng riderLocation, LatLng secondDriverLocation, Long ride) throws Exception {
    MobileRiderRideDto rideInfo = riderAction.getRideInfo(rider.getEmail(), ride);

    RiderRideAssert.assertThat(rideInfo)
      .hasRequestedCategory(TestUtils.REGULAR)
      .hasStatus(RideStatus.RIDER_CANCELLED)
      .hasStartLocation(riderLocation)
      .hasEndLocation(destination);
  }
}
