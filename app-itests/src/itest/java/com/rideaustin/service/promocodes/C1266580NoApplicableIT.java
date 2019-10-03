package com.rideaustin.service.promocodes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.rideaustin.model.ride.Ride;
import com.rideaustin.testrail.TestCases;

public class C1266580NoApplicableIT extends AbstractApplicableToFeesPromocodeTest {

  @Test
  @TestCases("C1266580")
  public void testNonApplicable() throws Exception {
    this.promocode = setup.getNonApplicablePromocode();
    doTest();
  }

  @Override
  protected void doAssert(Long ride) throws Exception {
    Ride rideInfo = rideDslRepository.findOne(ride);

    assertEquals(14.0, rideInfo.getTotalCharge().getAmount().doubleValue(), 0.0);
  }
}
