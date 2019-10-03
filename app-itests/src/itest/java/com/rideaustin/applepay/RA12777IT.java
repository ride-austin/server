package com.rideaustin.applepay;

import org.junit.Before;
import org.junit.Test;

import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.setup.RA12777Setup;

public class RA12777IT extends AbstractApplePayTest<RA12777Setup> {

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
    this.rider = setup.getNoCardRider();
  }

  @Test
  public void test() throws Exception {
    ActiveDriver driver = setup.getActiveDriver();
    Long ride = performRide(locationProvider.getCenter(), locationProvider.getOutsideAirportLocation(), driver, SAMPLE_TOKEN);
    paymentService.processRidePayment(ride);
  }

  @Override
  public Rider getRider() {
    return rider;
  }
}
