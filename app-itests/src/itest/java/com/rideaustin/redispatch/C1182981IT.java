package com.rideaustin.redispatch;

import com.google.maps.model.LatLng;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.rest.model.MobileRiderRideDto;
import com.rideaustin.test.asserts.RiderRideAssert;
import com.rideaustin.test.setup.DefaultRedispatchTestSetup;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertFalse;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * https://testrail.devfactory.com/index.php?/cases/view/1182981
 */
@Category(Redispatch.class)
public class C1182981IT extends AbstractRedispatchTest<DefaultRedispatchTestSetup> {

  @Test
  @TestCases("C1182981")
  public void test() throws Exception {
    doTestRedispatch(null);
  }

  @Override
  protected void cancelAndRedispatch(ActiveDriver firstDriver, ActiveDriver secondDriver, Long ride) throws Exception {
    String driverEmail = firstDriver.getDriver().getEmail();
    driverAction.reach(driverEmail, ride);
    driverAction.cancelRide(driverEmail, ride)
      .andExpect(status().isOk());

    //assert that rider received no notifications
    assertFalse("Rider should not be notified about cancellation", notificationFacade.getDataMap().keySet().contains(RideStatus.DRIVER_CANCELLED.name()));
  }

  @Override
  protected void assertRideRedispatched(LatLng destination, ActiveDriver secondDriver, LatLng riderLocation, LatLng secondDriverLocation, Long ride) throws Exception {
    awaitStatus(ride, RideStatus.DRIVER_CANCELLED);

    MobileRiderRideDto rideInfo = riderAction.getRideInfo(rider.getEmail(), ride);

    RiderRideAssert.assertThat(rideInfo)
      .hasRequestedCategory(TestUtils.REGULAR)
      .hasDriverAssigned(firstDriver.getId())
      .hasStatus(RideStatus.DRIVER_CANCELLED)
      .hasStartLocation(riderLocation)
      .hasEndLocation(destination);
  }
}
