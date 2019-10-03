package com.rideaustin.service.payment;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import com.google.maps.model.LatLng;
import com.rideaustin.Constants;
import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.filter.ClientType;
import com.rideaustin.jobs.RidePendingPaymentJob;
import com.rideaustin.model.enums.PaymentStatus;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Rider;
import com.rideaustin.payment.Payment;
import com.rideaustin.service.RiderCardService;
import com.rideaustin.service.email.InterceptingEmailService;
import com.rideaustin.service.email.InterceptingEmailService.Email;
import com.rideaustin.service.email.monitor.EmailCheckerService;
import com.rideaustin.service.thirdparty.StripeServiceMockImpl;
import com.rideaustin.test.AbstractNonTxTests;
import com.rideaustin.test.actions.DriverAction;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.actions.cascaded.CascadedDriverAction;
import com.rideaustin.test.asserts.EmailAssert;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.config.TestSetupConfig;
import com.rideaustin.test.setup.C1177104Setup;
import com.rideaustin.test.stubs.ConfigurationItemCache;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

@Category(Payment.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class, TestSetupConfig.class}, initializers = RAApplicationInitializer.class)
public class C1177104EmailAfterUnsuccessfulAttemptToChargeUnpaidRideIT extends AbstractNonTxTests<C1177104Setup> {

  @Inject
  private DriverAction driverAction;
  @Inject
  private RiderAction riderAction;

  @Inject
  private StripeServiceMockImpl mockedStripeService;
  @Inject
  private RiderCardService riderCardService;
  @Inject
  private EmailCheckerService emailCheckerService;

  @Inject
  private ConfigurationItemCache configurationItemCache;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    setup = createSetup();
    configurationItemCache.setBooleanConfig(Constants.DEFAULT_CITY_ID, ClientType.CONSOLE, "ridePayment", "asyncPreauthEnabled", false);
  }

  @Test
  @TestCases("C1177104")
  public void test() throws Exception {
    ActiveDriver activeDriver = setup.getActiveDriver();
    Date startDate = Date.from(LocalDateTime.now().minusSeconds(1)
      .atZone(ZoneOffset.systemDefault()).toInstant());

    Rider rider = setup.getRider();

    final LatLng defaultLocation = locationProvider.getCenter();

    driverAction.locationUpdate(activeDriver, defaultLocation.lat, defaultLocation.lng)
      .andExpect(status().isOk());
    Long rideId = riderAction.requestRide(rider.getEmail(), defaultLocation, TestUtils.REGULAR);

    awaitDispatch(activeDriver, rideId);

    CascadedDriverAction cascadedDriverAction = new CascadedDriverAction(activeDriver.getDriver(), driverAction).withRideId(rideId);

    cascadedDriverAction.acceptRide()
      .reach()
      .startRide()
      .endRide(defaultLocation.lat, defaultLocation.lng);

    riderCardService.lockCard(rider.getPrimaryCard(), rideDslRepository.findOne(rideId));
    prepareRideAsEndedAsUnpaid(rideId);
    mockedStripeService.setFailOnCardCharge(true);

    forceEndRide(rideId);

    schedulerService.triggerJob(RidePendingPaymentJob.class, "RidePendingPaymentJob", 1, new HashMap<>());
    schedulerService.executeNext();

    List<Email> recentMessages = fetchEmailsWithSleep();
    EmailAssert.assertThat(recentMessages).cardIsLocked(startDate, rider.getEmail());

  }

  private void prepareRideAsEndedAsUnpaid(Long rideId) {
    Ride ride = rideDslRepository.findOne(rideId);
    ride.setStatus(RideStatus.COMPLETED);
    ride.setPaymentStatus(PaymentStatus.UNPAID);
    rideDslRepository.save(ride);
  }

  private List<InterceptingEmailService.Email> fetchEmailsWithSleep() {
    sleeper.sleep(2000);
    return emailCheckerService.fetchEmails(5);
  }


}
