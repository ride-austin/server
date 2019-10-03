package com.rideaustin.service.promocodes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.rideaustin.model.ride.Ride;
import com.rideaustin.testrail.TestCases;

public class C1266578IT extends AbstractApplicableToFeesPromocodeTest {

  @Test
  @TestCases("C1266578")
  public void testApplicable() throws Exception {
    this.promocode = setup.getApplicablePromocode();
    doTest();
  }

  @Override
  protected void doAssert(Long ride) throws Exception {
    Ride rideInfo = rideDslRepository.findOne(ride);

    assertEquals(0.91, rideInfo.getRoundUpAmount().getAmount().doubleValue(), 0.0);
  }
}
