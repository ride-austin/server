package com.rideaustin.service.promocodes;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.google.maps.model.LatLng;
import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.promocodes.Promocode;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.service.config.RideDispatchServiceConfig;
import com.rideaustin.test.actions.cascaded.CascadedDriverAction;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.setup.RA12521Setup;
import com.rideaustin.test.stubs.transactional.PaymentService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public class RA12521ApplicableToFeeRideInDriverEarningsIT extends AbstractNonTxPromocodeTest<RA12521Setup> {

  private Promocode promocode;
  private Rider rider;
  private ActiveDriver activeDriver;

  @Inject
  protected RideDispatchServiceConfig config;
  @Inject
  protected PaymentService paymentService;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    setup = createSetup();
    this.rider = setup.getRider();
    this.activeDriver = setup.getActiveDriver();
    this.promocode = setup.getPromocode();
  }

  @Test
  public void test() throws Exception {
    LatLng pickupLocation = locationProvider.getCenter();
    LatLng dropoffLocation = locationProvider.getCenter();
    driverAction.locationUpdate(activeDriver, pickupLocation.lat, pickupLocation.lng)
      .andExpect(status().isOk());
    riderAction.usePromocode(rider, promocode)
      .andExpect(status().isOk());
    Long ride = riderAction.requestRide(rider.getEmail(), pickupLocation);

    awaitDispatch(activeDriver, ride);

    CascadedDriverAction cascadedDriverAction = new CascadedDriverAction(activeDriver.getDriver(), driverAction)
      .withRideId(ride);

    cascadedDriverAction.acceptRide()
      .reach()
      .startRide()
      .endRide(dropoffLocation.lat, dropoffLocation.lng);

    riderAction.rateRide(rider.getEmail(), ride, BigDecimal.ONE, BigDecimal.TEN, null)
      .andExpect(status().isOk());

    awaitStatus(ride, RideStatus.COMPLETED);

    paymentService.processRidePayment(ride);

    sleeper.sleep(5000);

    driverAction.requestEarningStats(activeDriver.getDriver(),
      Date.from(LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC)),
      Date.from(LocalDate.now().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content[0].totalFare").value("0.00"));
  }
}
