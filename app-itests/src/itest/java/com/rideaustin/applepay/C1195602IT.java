package com.rideaustin.applepay;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.junit.Test;

import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.user.Driver;
import com.rideaustin.service.email.InterceptingEmailService;
import com.rideaustin.service.email.monitor.EmailCheckerService;
import com.rideaustin.service.model.States;
import com.rideaustin.test.asserts.EmailAssert;
import com.rideaustin.test.setup.DefaultApplePaySetup;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

public class C1195602IT extends AbstractApplePayTest<DefaultApplePaySetup> {

  @Inject
  private EmailCheckerService emailCheckerService;

  @Test
  @TestCases("C1195602")
  public void test() throws Exception {
    Date date = new Date();
    Driver driver = setup.getActiveDriver().getDriver();

    driverAction.goOnline(driver.getUser().getEmail(), defaultLocation)
      .andExpect(status().isOk());

    Long rideId = riderAction.requestRide(rider.getEmail(), PICKUP_LOCATION,
      TestUtils.REGULAR, null, SAMPLE_TOKEN);

    sleeper.sleep(5000);

    riderAction.cancelRide(rider.getEmail(), rideId)
      .andExpect(status().isOk());

    awaitState(rideId, States.RIDER_CANCELLED, States.ENDED);
    awaitStatus(rideId, RideStatus.RIDER_CANCELLED);

    paymentService.processRidePayment(rideId);

    List<InterceptingEmailService.Email> emails = emailCheckerService.fetchEmails(5);

    EmailAssert.assertThat(emails)
      .cancellationDeliveredWithApplePayIcon(date, rider.getEmail());
  }
}
