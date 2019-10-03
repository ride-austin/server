package com.rideaustin.service.promocodes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.rideaustin.model.ride.Ride;
import com.rideaustin.testrail.TestCases;

public class C1266580ApplicableIT extends AbstractApplicableToFeesPromocodeTest {

  @Test
  @TestCases("C1266580")
  public void testApplicable() throws Exception {
    this.promocode = setup.getApplicablePromocode();
    doTest();
  }

  @Override
  protected void doAssert(Long ride) throws Exception {
    Ride rideInfo = rideDslRepository.findOne(ride);

    assertEquals(11.0, rideInfo.getTotalCharge().getAmount().doubleValue(), 0.0);
  }
}
