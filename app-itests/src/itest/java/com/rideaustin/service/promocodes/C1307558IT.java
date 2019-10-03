package com.rideaustin.service.promocodes;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.setup.C1307548Setup;
import com.rideaustin.testrail.TestCases;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public class C1307558IT extends BaseC1307548_53Test<C1307548Setup> {

  @Test
  @TestCases("C1307558")
  public void test() throws Exception {
    riderAction.usePromocode(rider, promocode)
      .andExpect(status().isOk());

    Long ride = rideAction.performRide(rider, locationProvider.getCenter(), locationProvider.getOutsideAirportLocation(), activeDriver);
    paymentService.processRidePayment(ride);

    Ride rideInfo = rideDslRepository.findOne(ride);

    assertEquals(5.0, rideInfo.getFreeCreditCharged().getAmount().doubleValue(), 0.0);
    assertEquals(0.34, rideInfo.getRoundUpAmount().getAmount().doubleValue(), 0.0);
  }
}
