package com.rideaustin.applepay;

import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.maps.model.LatLng;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.RiderCard;
import com.rideaustin.service.email.InterceptingEmailService;
import com.rideaustin.service.email.monitor.EmailCheckerService;
import com.rideaustin.test.asserts.EmailAssert;
import com.rideaustin.test.setup.C1181787Setup;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

@Category(ApplePay.class)
public class C1181787IT extends AbstractApplePayTest<C1181787Setup> {

  @Inject
  private EmailCheckerService emailCheckerService;

  @Test
  @TestCases("C1181787")
  public void test() throws Exception {
    Date start = new Date();
    LatLng pickupLocation = locationProvider.getCenter();
    LatLng dropoffLocation = locationProvider.getOutsideAirportLocation();
    ActiveDriver driver = setup.getActiveDriver();

    Long ride = performRide(pickupLocation, dropoffLocation, driver, SAMPLE_TOKEN);

    stripeServiceMock.setFailOnApplePayCharge(true);
    stripeServiceMock.setFailOnCardCharge(true);

    paymentService.processRidePayment(ride);

    List<InterceptingEmailService.Email> messages = emailCheckerService.fetchEmails(5);
    EmailAssert.assertThat(messages)
      .cardIsLocked(start, rider.getEmail())
      .paymentDeclined(start, rider.getEmail());

    RiderCard otherCard = setup.getOtherCard();
    otherCard.setPrimary(true);
    riderAction.setPrimaryCard(rider, otherCard)
      .andExpect(status().isOk());

    stripeServiceMock.resetFlags();

    Long newRide = riderAction.requestRide(rider.getEmail(), pickupLocation, TestUtils.REGULAR);

    assertNotNull(newRide);
  }
}
