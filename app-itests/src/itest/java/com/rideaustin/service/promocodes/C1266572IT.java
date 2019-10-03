package com.rideaustin.service.promocodes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.maps.model.LatLng;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.testrail.TestCases;

public class C1266572IT extends AbstractApplicableToFeesPromocodeTest {

  @Test
  @TestCases("C1266572")
  public void testApplicable() throws Exception {
    this.promocode = setup.getApplicablePromocode();
    doTest();
  }

  @Override
  protected LatLng getPickupLocation() {
    return locationProvider.getAirportLocation();
  }

  @Override
  protected void doAssert(Long ride) throws Exception {
    Ride rideInfo = rideDslRepository.findOne(ride);

    assertEquals(10.0, rideInfo.getTip().getAmount().doubleValue(), 0.0);
    assertEquals(0.91, rideInfo.getRoundUpAmount().getAmount().doubleValue(), 0.0);
    assertEquals(13.0, rideInfo.getTotalCharge().getAmount().doubleValue(), 0.0);

  }
}
