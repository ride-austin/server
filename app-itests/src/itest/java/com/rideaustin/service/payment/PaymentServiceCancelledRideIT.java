package com.rideaustin.service.payment;

import static com.rideaustin.test.util.TestUtils.money;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.time.DateUtils;
import org.joda.money.Money;
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
import com.rideaustin.model.ride.CityCarType;
import com.rideaustin.model.ride.FareDetails;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.jpa.RideRepository;
import com.rideaustin.service.CarTypeService;
import com.rideaustin.service.thirdparty.StripeServiceMockImpl;
import com.rideaustin.test.common.ITestProfileSupport;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.fixtures.RideFixture;
import com.rideaustin.test.stubs.ConfigurationItemCache;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public class PaymentServiceCancelledRideIT extends ITestProfileSupport {

  private static final String CAR_CATEGORY = "REGULAR";

  @Inject
  @Named("paymentService")
  private PaymentService testedInstance;

  @Inject
  private ConfigurationItemCache configurationItemCache;

  @Inject
  private RideRepository rideRepository;

  @Inject
  private CarTypeService carTypeService;

  @Inject
  private StripeServiceMockImpl stripeService;

  @Inject
  @Named("driverCancelledRide")
  private RideFixture cancelledRideFixture;

  private Ride cancelledRide;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    cancelledRide = cancelledRideFixture.getFixture();
    cancelledRide.setDriverReachedOn(DateUtils.addSeconds(new Date(), -30));
    rideRepository.save(cancelledRide);
    configurationItemCache.setIntConfig(Constants.DEFAULT_CITY_ID, ClientType.CONSOLE, "ridePayment", "cancellationChargeFreePeriod", 20);
  }

  @Test
  public void testProcessRidePayment_CancelledStripeSuccessful() throws Exception {

    long cancelledRideId = cancelledRide.getId();
    boolean paymentProcessed = testedInstance.processRidePayment(cancelledRideId);

    FareDetails fareDetails = rideRepository.findOne(cancelledRideId).getFareDetails();
    FareDetails expectedFare = cancelledFareDetails(true);

    assertEquals(expectedFare, fareDetails);
    assertTrue(paymentProcessed);
  }

  @Test
  public void testProcessRidePayment_CancelledStripeFail() throws Exception {
    stripeService.setFailOnCardCharge(true);

    long cancelledRideId = cancelledRide.getId();
    boolean paymentProcessed = testedInstance.processRidePayment(cancelledRideId);

    FareDetails fareDetails = rideRepository.findOne(cancelledRideId).getFareDetails();
    FareDetails expectedFare = cancelledFareDetails(false);

    assertEquals(expectedFare, fareDetails);
    assertFalse(paymentProcessed);
  }


  private FareDetails cancelledFareDetails(boolean charged) {
    Optional<CityCarType> carType = carTypeService.getCityCarType(CAR_CATEGORY, 1L);
    Money cancellationFee = carType.get().getCancellationFee();
    return FareDetails.builder()
      .minimumFare(money(0.00))
      .baseFare(money(0.0))
      .ratePerMile(money(0.0))
      .ratePerMinute(money(0.0))
      .estimatedFare(null)
      .bookingFee(money(0.0))
      .distanceFare(money(0.0))
      .timeFare(money(0.0))
      .cityFee(money(0.0))
      .cancellationFee(cancellationFee)
      .processingFee(money(0.0))
      .subTotal(cancellationFee)
      .normalFare(money(0.0))
      .surgeFare(money(0.0))
      .totalFare(cancellationFee)
      .freeCreditCharged(money(0.0))
      .stripeCreditCharge(charged ? cancellationFee : money(0.0))
      .driverPayment(cancellationFee)
      .raPayment(money(0))
      .tip(money(0))
      .roundUpAmount(money(0.0))
      .airportFee(money(0.0))
      .build();
  }

}