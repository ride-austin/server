package com.rideaustin.service.promocodes;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.rest.model.MobileRiderRideDto;
import com.rideaustin.test.asserts.RiderRideAssert;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.setup.C1307553Setup;
import com.rideaustin.testrail.TestCases;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public class C1307553IT extends BaseC1307548_53Test<C1307553Setup> {

  @Test
  @TestCases("C1307553")
  public void test() throws Exception {
    riderAction.usePromocode(rider, promocode)
      .andExpect(status().isOk());

    for (int i = 0; i < 4; i++) {
      Long ride = rideAction.performRide(rider, locationProvider.getCenter(), locationProvider.getCenter(), activeDriver);

      awaitStatus(ride, RideStatus.COMPLETED);

      paymentService.processRidePayment(ride);
      Ride rideInfo = rideDslRepository.findOne(ride);
      assertEquals(promocode.getCappedAmountPerUse().min(rideInfo.getRideCost().minus(rideInfo.getCityFee()).getAmount()).doubleValue(), rideInfo.getFreeCreditCharged().getAmount().doubleValue(), 0.0);
    }

    Long ride = rideAction.performRide(rider, locationProvider.getCenter(), locationProvider.getOutsideAirportLocation(), activeDriver);
    paymentService.processRidePayment(ride);

    MobileRiderRideDto rideInfo = riderAction.getRideInfo(rider.getEmail(), ride);
    RiderRideAssert.assertThat(rideInfo)
      .hasFreeCreditCharged(BigDecimal.ZERO);
  }

  @After
  @Override
  public void supportTearDown() {
    super.supportTearDown();
    carTypesCache.refreshCache();
  }
}
