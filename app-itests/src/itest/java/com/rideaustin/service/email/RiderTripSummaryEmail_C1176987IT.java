package com.rideaustin.service.email;

import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.asserts.EmailAssert;
import com.rideaustin.test.setup.C1176987Setup;
import com.rideaustin.testrail.TestCases;

@Category(Email.class)
public class RiderTripSummaryEmail_C1176987IT extends AbstractTripSummaryEmailNonTxTest<C1176987Setup> {

  @Test
  @TestCases("C1176987")
  public void shouldSendTripSummaryEmail_WithSurgeFactorAsOne() throws Exception {
    final Rider rider = setup.getRider();
    final ActiveDriver activeDriver = setup.getActiveDriver();

    final Long rideId = rideAction.performRide(rider, locationProvider.getRandomLocation(), locationProvider.getCenter(), activeDriver);

    paymentService.processRidePayment(rideId);

    List<InterceptingEmailService.Email> recentMessages = fetchEmailsWithSleep();
    EmailAssert.assertThat(recentMessages).tripSummaryEmailDeliveredWithoutPF(startDate, rider.getEmail());
  }
}
