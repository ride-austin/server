package com.rideaustin.dispatch;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.maps.model.LatLng;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.user.Rider;
import com.rideaustin.redispatch.Redispatch;
import com.rideaustin.rest.model.MobileRiderRideDto;
import com.rideaustin.test.asserts.RiderRideAssert;
import com.rideaustin.test.setup.DefaultRedispatchTestSetup;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

@Category(Redispatch.class)
public class C1177070NoAvailableDriversIT extends BaseDispatchTest<DefaultRedispatchTestSetup> {

  private LatLng defaultClosestLocation = new LatLng(30.269372, -97.740394);

  private Rider rider;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    setup = createSetup();
    rider = setup.getRider();
  }

  @Test
  @TestCases("C1177070")
  public void test() throws Exception {
    Long rideId = riderAction.requestRide(rider.getEmail(), defaultClosestLocation, TestUtils.REGULAR);

    awaitStatus(rideId, RideStatus.NO_AVAILABLE_DRIVER);

    assertRideStatus(rider.getEmail(), rideId, RideStatus.NO_AVAILABLE_DRIVER);
  }

  private void assertRideStatus(String riderEmail, Long rideId, RideStatus status) throws Exception {
    MobileRiderRideDto rideInfo = riderAction.getRideInfo(riderEmail, rideId);

    RiderRideAssert.assertThat(rideInfo)
      .hasStatus(status);
  }
}
