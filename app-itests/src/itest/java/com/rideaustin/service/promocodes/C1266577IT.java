package com.rideaustin.service.promocodes;

import static com.rideaustin.test.util.TestUtils.money;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.maps.model.LatLng;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.testrail.TestCases;

import org.junit.Test;

public class C1266577IT extends AbstractApplicableToFeesPromocodeTest {

  @Test
  @TestCases("C1266577")
  public void testApplicable() throws Exception {
    this.promocode = setup.getApplicablePromocode();
    doTest();
  }

  @Test
  @TestCases("C1266577")
  public void testNonApplicable() throws Exception {
    this.promocode = setup.getNonApplicablePromocode();
    doTest();
  }

  @Override
  protected LatLng getPickupLocation() {
    return locationProvider.getAirportLocation();
  }

  @Override
  protected void doAssert(Long ride) throws Exception {
    Ride rideInfo = rideDslRepository.findOne(ride);

    assertTrue(rideInfo.getTotalCharge().isGreaterThan(money(1.0)));
    assertEquals(2.0, rideInfo.getAirportFee().getAmount().doubleValue(), 0.0);
  }
}
