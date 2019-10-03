package com.rideaustin.payment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.inject.Inject;

import org.joda.money.Money;
import org.junit.Test;

import com.rideaustin.model.enums.PaymentStatus;
import com.rideaustin.model.promocodes.Promocode;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.setup.UpfrontPricingPromocodeSetup;

public class UpfrontPricingPromocodeIT extends AbstractUpfrontPricingTest<UpfrontPricingPromocodeSetup> {

  @Inject
  private RiderAction riderAction;

  @Test
  public void test() throws Exception {
    final Rider rider = setup.getRider();
    final Promocode promocode = setup.getPromocode();
    final ActiveDriver activeDriver = setup.getActiveDriver();

    riderAction.usePromocode(rider, promocode)
      .andDo(print())
      .andExpect(status().isOk());

    final Long rideId = rideAction.performRide(rider, locationProvider.getCenter(),
      locationProvider.getOutsideAirportLocation(), activeDriver, 3000);

    assertUpfrontCharge(rideId);

    Ride ride = rideDslRepository.findOne(rideId);
    final Money prepaid = ride.getStripeCreditCharge();

    paymentService.processRidePayment(rideId);

    ride = rideDslRepository.findOne(rideId);
    assertNotNull(ride.getFreeCreditCharged());
    System.out.println(ride.getFreeCreditCharged());
    assertEquals(PaymentStatus.PAID, ride.getPaymentStatus());
    assertEquals(prepaid, ride.getStripeCreditCharge());
  }
}
