package com.rideaustin.dispatch;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.redispatch.Redispatch;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

@Category(Redispatch.class)
public class C1177071DispatchToNextAfterFirstMissedIT extends BaseC1177071Test {

  @Test
  @TestCases("C1177071")
  public void test() throws Exception {

    driverAction.locationUpdate(firstActiveDriver, defaultClosestLocation.lat, defaultClosestLocation.lng);
    driverAction.locationUpdate(secondActiveDriver, defaultNotClosestLocation.lat, defaultNotClosestLocation.lng);

    Long rideId = riderAction.requestRide(rider.getEmail(), defaultClosestLocation, TestUtils.REGULAR);

    awaitDispatch(secondActiveDriver, rideId);
  }
}
