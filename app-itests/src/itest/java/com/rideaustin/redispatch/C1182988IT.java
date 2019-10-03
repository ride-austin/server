package com.rideaustin.redispatch;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.maps.model.LatLng;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.test.asserts.DispatchHistoryAssert;
import com.rideaustin.test.setup.DefaultRedispatchTestSetup;
import com.rideaustin.testrail.TestCases;

/**
 * https://testrail.devfactory.com/index.php?/cases/view/1182988
 */
@Category(Redispatch.class)
public class C1182988IT extends AbstractRedispatchTest<DefaultRedispatchTestSetup> {

  @Test
  @TestCases("C1182988")
  public void test() throws Exception {
    doTestRedispatch(null);
  }

  @Override
  protected void assertRideRedispatched(LatLng destination, ActiveDriver secondDriver, LatLng riderLocation, LatLng secondDriverLocation, Long ride) throws Exception {
    super.assertRideRedispatched(destination, secondDriver, riderLocation, secondDriverLocation, ride);

    DispatchHistoryAssert.assertThat(getDispatchHistory(ride))
      .hasLength(2)
      .isDispatchedFirstTo(firstDriver.getId())
      .thenIsDispatchedTo(secondDriver.getId());
  }
}
