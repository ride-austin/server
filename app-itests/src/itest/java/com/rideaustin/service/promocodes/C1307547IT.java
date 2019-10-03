package com.rideaustin.service.promocodes;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.model.promocodes.Promocode;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.rest.model.MobileRiderRideDto;
import com.rideaustin.service.payment.PaymentService;
import com.rideaustin.test.actions.RideAction;
import com.rideaustin.test.asserts.RiderRideAssert;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.setup.C1307547Setup;
import com.rideaustin.testrail.TestCases;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public class C1307547IT extends AbstractNonTxPromocodeTest<C1307547Setup> {

  private Rider rider;
  private ActiveDriver activeDriver;
  private Promocode promocode;

  @Inject
  private RideAction rideAction;

  @Inject
  private PaymentService paymentService;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    setup = createSetup();
    rider = setup.getRider();
    activeDriver = setup.getActiveDriver();
    promocode = setup.getPromocode();
  }

  @Test
  @TestCases("C1307547")
  public void test() throws Exception {
    riderAction.usePromocode(rider, promocode)
      .andExpect(status().isOk());

    for (int i = 0; i < 4; i++) {
      Long ride = rideAction.performRide(rider, locationProvider.getCenter(), locationProvider.getOutsideAirportLocation(), activeDriver);
      paymentService.processRidePayment(ride);
      MobileRiderRideDto rideInfo = riderAction.getRideInfo(rider.getEmail(), ride);
      RiderRideAssert.assertThat(rideInfo)
        .hasFreeCreditCharged(promocode.getCappedAmountPerUse());
    }

    Long ride = rideAction.performRide(rider, locationProvider.getCenter(), locationProvider.getOutsideAirportLocation(), activeDriver);
    paymentService.processRidePayment(ride);

    MobileRiderRideDto rideInfo = riderAction.getRideInfo(rider.getEmail(), ride);
    RiderRideAssert.assertThat(rideInfo)
      .hasFreeCreditCharged(BigDecimal.ZERO);
  }
}