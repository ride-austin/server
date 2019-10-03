package com.rideaustin.service.email;

import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.asserts.EmailAssert;
import com.rideaustin.test.setup.C1176993Setup;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

@Category(Email.class)
public class RiderTripSummaryEmail_C1176993IT extends AbstractTripSummaryEmailNonTxTest<C1176993Setup> {

  @Test
  @TestCases("C1176993")
  public void shouldSendTripSummaryEmail_WithCarTypeAsRegular() throws Exception {
    doTest(TestUtils.REGULAR);
  }

  @Test
  @TestCases("C1176993")
  public void shouldSendTripSummaryEmail_WithCarTypeAsSUV() throws Exception {
    doTest(TestUtils.SUV);
  }

  @Test
  @TestCases("C1176993")
  public void shouldSendTripSummaryEmail_WithCarTypeAsPremium() throws Exception {
    doTest(TestUtils.PREMIUM);
  }

  private void doTest(String premium) throws Exception {
    final Rider rider = setup.getRider();
    final ActiveDriver activeDriver = setup.getActiveDriver();

    final Long rideId = rideAction.performRide(rider, locationProvider.getRandomLocation(), locationProvider.getCenter(), activeDriver, premium);

    paymentService.processRidePayment(rideId);

    final Ride ride = rideDslRepository.findOne(rideId);

    List<InterceptingEmailService.Email> recentMessages = fetchEmailsWithSleep();
    EmailAssert.assertThat(recentMessages).tripSummaryEmailDeliveredWithCarType(startDate, rider.getEmail(), ride);
  }

}
