package com.rideaustin.service.promocodes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import javax.inject.Inject;

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
import com.rideaustin.repo.dsl.PromocodeRedemptionDslRepository;
import com.rideaustin.service.CityCache;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.setup.C1307548Setup;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public class RA12289IT extends BaseC1307548_53Test<C1307548Setup> {

  @Inject
  private PromocodeRedemptionDslRepository redemptionDslRepository;

  @Inject
  private CityCache cityCache;

  @Test
  public void test() throws Exception {
    riderAction.usePromocode(rider, promocode)
      .andExpect(status().isOk());

    Long ride = rideAction.performRide(rider, locationProvider.getCenter(), locationProvider.getOutsideAirportLocation(), activeDriver);

    awaitStatus(ride, RideStatus.COMPLETED);

    paymentService.processRidePayment(ride);

    Ride rideInfo = rideDslRepository.findOne(ride);

    assertEquals(promocode.getCappedAmountPerUse().doubleValue(), rideInfo.getFreeCreditCharged().getAmount().doubleValue(), 0.0);

    BigDecimal remainingSumFor = redemptionDslRepository.getRemainingSumForRider(rider.getId(), cityCache.getCity(rider.getCityId()));
    assertThat(remainingSumFor).isEqualByComparingTo(promocode.getCodeValue().subtract(promocode.getCappedAmountPerUse()));
  }

}
