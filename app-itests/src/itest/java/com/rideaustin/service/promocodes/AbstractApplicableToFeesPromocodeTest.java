package com.rideaustin.service.promocodes;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.google.maps.model.LatLng;
import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.model.promocodes.Promocode;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.service.config.RideDispatchServiceConfig;
import com.rideaustin.test.AbstractNonTxTests;
import com.rideaustin.test.actions.DriverAction;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.actions.cascaded.CascadedDriverAction;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.setup.DefaultApplicableToFeesPromocodeSetup;
import com.rideaustin.test.stubs.transactional.PaymentService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public abstract class AbstractApplicableToFeesPromocodeTest extends AbstractNonTxTests<DefaultApplicableToFeesPromocodeSetup> {

  @Inject
  protected RiderAction riderAction;
  @Inject
  protected DriverAction driverAction;

  @Inject
  protected RideDispatchServiceConfig config;
  @Inject
  protected PaymentService paymentService;
  protected Rider rider;
  protected ActiveDriver activeDriver;
  protected Promocode promocode;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    this.setup = createSetup();
    this.rider = setup.getRider();
    this.activeDriver = setup.getActiveDriver();
  }

  protected void doTest() throws Exception {
    LatLng pickupLocation = getPickupLocation();
    LatLng dropoffLocation = locationProvider.getOutsideAirportLocation();
    driverAction.locationUpdate(activeDriver, pickupLocation.lat, pickupLocation.lng)
      .andExpect(status().isOk());
    riderAction.usePromocode(rider, promocode)
      .andDo(print())
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

    paymentService.processRidePayment(ride);

    doAssert(ride);
  }

  protected LatLng getPickupLocation() {
    return locationProvider.getCenter();
  }

  protected abstract void doAssert(Long ride) throws Exception;
}
