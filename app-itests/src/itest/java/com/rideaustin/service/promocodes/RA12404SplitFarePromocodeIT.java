package com.rideaustin.service.promocodes;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.google.common.collect.ImmutableSet;
import com.google.maps.model.LatLng;
import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.promocodes.Promocode;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Rider;
import com.rideaustin.service.config.RideDispatchServiceConfig;
import com.rideaustin.test.actions.cascaded.CascadedDriverAction;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.setup.RA12404Setup;
import com.rideaustin.test.stubs.transactional.PaymentService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public class RA12404SplitFarePromocodeIT extends AbstractNonTxPromocodeTest<RA12404Setup> {
  
  @Inject
  private RideDispatchServiceConfig config;
  @Inject
  private PaymentService paymentService;
  private Rider mainRider;
  private Rider secondRider;
  private ActiveDriver activeDriver;
  private Promocode promocode;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    setup = createSetup();
    this.mainRider = setup.getMainRider();
    this.secondRider = setup.getSecondRider();
    this.activeDriver = setup.getActiveDriver();
    this.promocode = setup.getPromocode();
  }

  @Test
  public void test() throws Exception {
    LatLng pickupLocation = locationProvider.getCenter();
    LatLng dropoffLocation = locationProvider.getOutsideAirportLocation();
    riderAction.usePromocode(mainRider, promocode)
      .andExpect(status().isOk());
    driverAction.locationUpdate(activeDriver, pickupLocation.lat, pickupLocation.lng)
      .andExpect(status().isOk());
    Long ride = riderAction.requestRide(mainRider.getEmail(), pickupLocation);

    awaitDispatch(activeDriver, ride);

    Long splitFare = riderAction.requestSplitFare(mainRider.getEmail(), ride, ImmutableSet.of(secondRider.getPhoneNumber()));

    riderAction.acceptSplitFare(secondRider.getEmail(), splitFare)
      .andExpect(status().isOk());

    CascadedDriverAction cascadedDriverAction = new CascadedDriverAction(activeDriver.getDriver(), driverAction)
      .withRideId(ride);

    cascadedDriverAction.acceptRide()
      .reach()
      .startRide()
      .endRide(dropoffLocation.lat, dropoffLocation.lng);

    awaitStatus(ride, RideStatus.COMPLETED);

    riderAction.rateRide(mainRider.getEmail(), ride, BigDecimal.ONE, BigDecimal.TEN, null)
      .andExpect(status().isOk());

    paymentService.processRidePayment(ride);

    Ride rideInfo = rideDslRepository.findOne(ride);

    assertEquals(11.0, rideInfo.getTotalCharge().getAmount().doubleValue(), 0.0);
  }
}
