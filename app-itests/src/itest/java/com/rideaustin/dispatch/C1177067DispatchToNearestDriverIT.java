package com.rideaustin.dispatch;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.maps.model.LatLng;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.redispatch.Redispatch;
import com.rideaustin.test.setup.DefaultRedispatchTestSetup;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

@Category(Redispatch.class)
public class C1177067DispatchToNearestDriverIT extends BaseDispatchTest<DefaultRedispatchTestSetup> {

  private LatLng defaultClosestLocation = new LatLng(30.269372, -97.740394);
  private LatLng defaultNotClosestLocation = new LatLng(30.263372, -97.744394);

  private Rider rider;
  private ActiveDriver firstActiveDriver;
  private ActiveDriver secondActiveDriver;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    rider = this.setup.getRider();
    firstActiveDriver = this.setup.getFirstActiveDriver();
    secondActiveDriver = this.setup.getSecondActiveDriver();
  }

  @Test
  @TestCases("C1177067")
  public void test() throws Exception {

    driverAction.locationUpdate(firstActiveDriver, defaultClosestLocation.lat, defaultClosestLocation.lng);
    driverAction.locationUpdate(secondActiveDriver, defaultNotClosestLocation.lat, defaultNotClosestLocation.lng);

    Long rideId = riderAction.requestRide(rider.getEmail(), defaultClosestLocation, TestUtils.REGULAR);

    awaitDispatch(firstActiveDriver, rideId);

    driverAction.acceptRide(firstActiveDriver, rideId);
    awaitStatus(rideId, RideStatus.DRIVER_ASSIGNED);
  }
}
