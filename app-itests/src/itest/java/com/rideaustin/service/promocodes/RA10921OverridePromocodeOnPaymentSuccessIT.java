package com.rideaustin.service.promocodes;

import static org.junit.Assert.assertEquals;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.model.promocodes.PromocodeRedemption;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.service.payment.PaymentService;
import com.rideaustin.test.common.ITestProfileSupport;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.fixtures.PromocodeRedemptionFixture;
import com.rideaustin.test.fixtures.RideFixture;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public class RA10921OverridePromocodeOnPaymentSuccessIT extends ITestProfileSupport {

  @Inject
  private PaymentService paymentService;

  @Inject
  @Named("completedRide")
  private RideFixture rideFixture;
  private Ride ride;

  @Inject
  @Named("validRedemption")
  private PromocodeRedemptionFixture validRedemption;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    ride = rideFixture.getFixture();
  }

  @Test
  public void testPromocodeSuccess() throws Exception {
    PromocodeRedemption redemption = validRedemption.getFixture();

    paymentService.processRidePayment(ride.getId());

    assertEquals((Long) redemption.getId(), ride.getPromocodeRedemptionId());
    assertEquals(0, redemption.getPromocode().getCodeValue().compareTo(ride.getFreeCreditCharged().getAmount()));
  }

}
