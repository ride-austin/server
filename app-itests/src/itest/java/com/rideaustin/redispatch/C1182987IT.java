package com.rideaustin.redispatch;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.maps.model.LatLng;
import com.rideaustin.model.Document;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.service.DocumentService;
import com.rideaustin.service.email.InterceptingEmailService;
import com.rideaustin.service.email.monitor.EmailCheckerService;
import com.rideaustin.service.payment.PaymentService;
import com.rideaustin.test.asserts.EmailAssert;
import com.rideaustin.test.common.Sleeper;
import com.rideaustin.test.setup.DefaultRedispatchTestSetup;
import com.rideaustin.testrail.TestCases;

/**
 * https://testrail.devfactory.com/index.php?/cases/view/1182987
 */
@Category(Redispatch.class)
public class C1182987IT extends AbstractRedispatchTest<DefaultRedispatchTestSetup> {

  @Inject
  private EmailCheckerService emailCheckerService;

  @Inject
  private PaymentService paymentService;

  @Inject
  private DocumentService documentService;

  @Inject
  private Sleeper sleeper;

  private LatLng destination;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    destination = locationProvider.getAirportLocation();
  }

  @Test
  @TestCases("C1182987")
  public void test() throws Exception {
    doTestRedispatch(destination);
  }

  @Override
  protected void acceptRedispatched(ActiveDriver driver, Long ride) throws Exception {
    super.acceptRedispatched(driver, ride);
    driverAction.reach(driver.getDriver().getEmail(), ride)
      .andExpect(status().isOk());
    driverAction.startRide(driver.getDriver().getEmail(), ride)
      .andExpect(status().isOk());
    driverAction.endRide(driver.getDriver().getEmail(), ride, destination.lat, destination.lng)
      .andExpect(status().isOk());
  }

  @Override
  protected void assertRideRedispatched(LatLng destination, ActiveDriver secondDriver, LatLng riderLocation, LatLng secondDriverLocation, Long ride) throws Exception {
    super.assertRideRedispatched(destination, secondDriver, riderLocation, secondDriverLocation, ride);

    Date processedDate = new Date();

    awaitStatus(ride, RideStatus.COMPLETED);

    paymentService.processRidePayment(ride);

    Document photo = documentService.findAvatarDocument(secondDriver.getDriver(), DocumentType.DRIVER_PHOTO);
    String photoUrl = photo.getDocumentUrl();

    sleeper.sleep(2000);
    List<InterceptingEmailService.Email> messages = emailCheckerService.fetchEmails(5);
    EmailAssert.assertThat(messages)
      .tripSummaryEmailDeliveredWithDriverPhoto(processedDate, rider.getEmail(), photoUrl);
  }

  @After
  public void tearDown() throws Exception {
    super.supportTearDown();
    emailCheckerService.close();
  }
}
