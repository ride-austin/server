package com.rideaustin.applepay;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.junit.Test;

import com.google.maps.model.LatLng;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.service.email.InterceptingEmailService;
import com.rideaustin.service.email.monitor.EmailCheckerService;
import com.rideaustin.test.asserts.EmailAssert;
import com.rideaustin.test.setup.DefaultApplePaySetup;
import com.rideaustin.testrail.TestCases;

public class C1181767IT extends AbstractApplePayTest<DefaultApplePaySetup> {

  @Inject
  private EmailCheckerService emailCheckerService;

  @Test
  @TestCases("C1181767")
  public void test() throws Exception {
    Date start = new Date();
    LatLng pickupLocation = locationProvider.getCenter();
    LatLng dropoffLocation = locationProvider.getOutsideAirportLocation();
    ActiveDriver driver = setup.getActiveDriver();

    Long ride = performRide(pickupLocation, dropoffLocation, driver, SAMPLE_TOKEN);

    paymentService.processRidePayment(ride);

    List<InterceptingEmailService.Email> emails = emailCheckerService.fetchEmails(5);

    EmailAssert.assertThat(emails)
      .tripSummaryEmailDeliveredWithApplePayIcon(start, rider.getEmail());
  }
}
