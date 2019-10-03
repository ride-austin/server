package com.rideaustin.payment;

import static org.junit.Assert.assertEquals;

import org.joda.money.Money;
import org.junit.Test;

import com.rideaustin.model.enums.PaymentStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.test.setup.UpfrontPricingSetup;

public class UpfrontPricingIT extends AbstractUpfrontPricingTest<UpfrontPricingSetup> {

  @Test
  public void test() throws Exception {
    final Long rideId = rideAction.performRide(setup.getRider(), locationProvider.getCenter(),
      locationProvider.getOutsideAirportLocation(), setup.getActiveDriver(), 3000);

    assertUpfrontCharge(rideId);

    Ride ride = rideDslRepository.findOne(rideId);
    final Money prepaid = ride.getStripeCreditCharge();

    paymentService.processRidePayment(rideId);

    ride = rideDslRepository.findOne(rideId);
    assertEquals(PaymentStatus.PAID, ride.getPaymentStatus());
    assertEquals(prepaid, ride.getStripeCreditCharge());
  }

}
