package com.rideaustin.service.promocodes;

import static com.rideaustin.test.util.TestUtils.money;
import static org.junit.Assert.assertEquals;

import com.google.maps.model.LatLng;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.rest.model.MobileRiderRideDto;
import com.rideaustin.test.asserts.RiderRideAssert;
import com.rideaustin.testrail.TestCases;

import org.junit.Test;

public class C1177086IT extends AbstractApplicableToFeesPromocodeTest {

  @Test
  @TestCases("C1177086")
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
    MobileRiderRideDto rideInfo = riderAction.getRideInfo(rider.getEmail(), ride);

    RiderRideAssert.assertThat(rideInfo)
      .hasTip(10.0);

    Ride rideEntity = rideDslRepository.findOne(ride);
    assertEquals(money(0.91), rideEntity.getRoundUpAmount());
    assertEquals(money(16), rideEntity.getTotalCharge());
  }
}
