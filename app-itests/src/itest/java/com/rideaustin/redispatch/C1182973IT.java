package com.rideaustin.redispatch;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.maps.model.LatLng;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.test.setup.DefaultRedispatchTestSetup;
import com.rideaustin.test.stubs.transactional.PaymentService;
import com.rideaustin.testrail.TestCases;

/**
 * https://testrail.devfactory.com/index.php?/cases/view/1182973
 */
@Category(Redispatch.class)
public class C1182973IT extends AbstractRedispatchTest<DefaultRedispatchTestSetup> {

  @Inject
  private PaymentService paymentService;

  @Test
  @TestCases("C1182973")
  public void test() throws Exception {
    doTestRedispatch(null);
  }

  @Override
  protected void assertRideRedispatched(LatLng destination, ActiveDriver secondDriver, LatLng riderLocation, LatLng secondDriverLocation, Long ride) throws Exception {
    super.assertRideRedispatched(destination, secondDriver, riderLocation, secondDriverLocation, ride);

    forceEndRide(ride);
    execute(() -> {
      try {
        paymentService.processRidePayment(ride);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

  }
}
