package com.rideaustin.service.email;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.asserts.EmailAssert;
import com.rideaustin.test.setup.C1176987Setup;
import com.rideaustin.testrail.TestCases;

@Category(Email.class)
public class RiderTripSummaryEmail_C1176989IT extends AbstractTripSummaryEmailNonTxTest<C1176987Setup> {

  private Double tip = 1.0;

  @Inject
  private RiderAction riderAction;

  @Test
  @TestCases("C1176989")
  public void shouldSendTripSummaryEmail_WithTip() throws Exception {
    final Rider rider = setup.getRider();
    final ActiveDriver activeDriver = setup.getActiveDriver();

    final Long rideId = rideAction.performRide(rider, locationProvider.getRandomLocation(), locationProvider.getCenter(), activeDriver);

    riderAction.rateRide(rider.getEmail(), rideId, BigDecimal.valueOf(5), BigDecimal.valueOf(tip), "Comment");

    paymentService.processRidePayment(rideId);

    List<InterceptingEmailService.Email> recentMessages = fetchEmailsWithSleep();
    EmailAssert.assertThat(recentMessages).tripSummaryEmailDeliveredWithTip(startDate, rider.getEmail());
  }

}
