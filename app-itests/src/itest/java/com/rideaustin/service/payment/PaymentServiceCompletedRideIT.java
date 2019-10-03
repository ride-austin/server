package com.rideaustin.service.payment;

import static com.rideaustin.test.util.TestUtils.money;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.rideaustin.Constants;
import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.ride.FareDetails;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.jpa.RideRepository;
import com.rideaustin.service.thirdparty.StripeServiceMockImpl;
import com.rideaustin.test.common.ITestProfileSupport;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.fixtures.RideFixture;
import com.rideaustin.test.stubs.ConfigurationItemCache;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public class PaymentServiceCompletedRideIT extends ITestProfileSupport {

  @Inject
  @Named("paymentService")
  private PaymentService testedInstance;

  @Inject
  private ConfigurationItemCache configurationItemCache;

  @Inject
  private RideRepository rideRepository;

  @Inject
  private StripeServiceMockImpl stripeService;

  @Inject
  @Named("completedRide")
  private RideFixture completedRideFixture;

  private Ride completedRide;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    completedRide = completedRideFixture.getFixture();
    configurationItemCache.setIntConfig(Constants.DEFAULT_CITY_ID, ClientType.CONSOLE, "ridePayment", "cancellationChargeFreePeriod", 20);
  }

  @Test
  public void testProcessRidePayment_TippedCompletedStripeSuccessful() throws Exception {

    long completedRideId = completedRide.getId();
    boolean paymentProcessed = testedInstance.processRidePayment(completedRideId);

    FareDetails fareDetails = rideRepository.findOne(completedRideId).getFareDetails();
    FareDetails expectedFare = tippedCompletedFareDetails(12.0);

    assertEquals(expectedFare, fareDetails);
    assertTrue(paymentProcessed);
  }

  @Test
  public void testProcessRidePayment_TippedCompletedStripeFailed() throws Exception {
    stripeService.setFailOnCardCharge(true);

    long completedRideId = completedRide.getId();
    boolean paymentProcessed = testedInstance.processRidePayment(completedRideId);

    FareDetails fareDetails = rideRepository.findOne(completedRideId).getFareDetails();
    FareDetails expectedFare = tippedCompletedFareDetails(0.0);

    assertEquals(expectedFare, fareDetails);
    assertFalse(paymentProcessed);
  }

  private FareDetails tippedCompletedFareDetails(double charge) {
    return FareDetails.builder()
      .minimumFare(money(5.00))
      .baseFare(money(1.5))
      .ratePerMile(money(.99))
      .ratePerMinute(money(.25))
      .estimatedFare(null)
      .bookingFee(money(2.0))
      .distanceFare(money(1.15))
      .timeFare(money(.07))
      .cityFee(money(0.07))
      .cancellationFee(money(0.0))
      .processingFee(money(1.0))
      .subTotal(money(5))
      .normalFare(money(5))
      .surgeFare(money(0))
      .totalFare(money(8.07))
      .freeCreditCharged(money(0.0))
      .stripeCreditCharge(money(charge))
      .driverPayment(money(8))
      .raPayment(money(0))
      .tip(money(3))
      .roundUpAmount(money(0.93))
      .airportFee(money(0.0))
      .build();
  }

}