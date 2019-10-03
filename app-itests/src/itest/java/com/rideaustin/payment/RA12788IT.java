package com.rideaustin.payment;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import com.google.common.collect.ImmutableSet;
import com.google.maps.model.LatLng;
import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.service.thirdparty.StripeServiceMockImpl;
import com.rideaustin.test.AbstractNonTxTests;
import com.rideaustin.test.actions.DriverAction;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.actions.cascaded.CascadedDriverAction;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.config.TestSetupConfig;
import com.rideaustin.test.setup.RA12788Setup;
import com.rideaustin.test.stubs.transactional.PaymentService;
import com.rideaustin.test.util.TestUtils;

@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class, TestSetupConfig.class}, initializers = RAApplicationInitializer.class)
public class RA12788IT extends AbstractNonTxTests<RA12788Setup> {

  private Rider mainRider;
  private Rider secondRider;
  private ActiveDriver driver;

  @Inject
  private PaymentService paymentService;

  @Inject
  private StripeServiceMockImpl stripeServiceMock;

  @Inject
  private RiderAction riderAction;
  @Inject
  private DriverAction driverAction;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    setup = createSetup();
    mainRider = setup.getMainRider();
    secondRider = setup.getSecondRider();
    driver = setup.getDriver();
  }

  @Test
  public void test() throws Exception {
    Long ride = performRide(locationProvider.getCenter(), locationProvider.getOutsideAirportLocation(), driver);

    awaitStatus(ride, RideStatus.COMPLETED);

    stripeServiceMock.setFailedRider(mainRider);
    paymentService.processRidePayment(ride);

    riderAction.getPendingPayments(mainRider)
      .andExpect(status().isOk())
      .andDo(print());

    riderAction.getPendingPayments(secondRider)
      .andExpect(status().isOk())
      .andDo(print());
  }

  protected Long performRide(LatLng pickupLocation, LatLng dropoffLocation, ActiveDriver driver) throws Exception {
    driverAction.goOnline(driver.getDriver().getEmail(), pickupLocation)
      .andExpect(status().isOk());

    Long ride = riderAction.requestRide(mainRider.getEmail(), pickupLocation, TestUtils.REGULAR);

    awaitDispatch(driver, ride);

    Long splitFare = riderAction.requestSplitFare(mainRider.getEmail(), ride, ImmutableSet.of(secondRider.getPhoneNumber()));
    riderAction.acceptSplitFare(secondRider.getEmail(), splitFare)
      .andExpect(status().isOk());

    CascadedDriverAction cascadedDriverAction = new CascadedDriverAction(driver.getDriver(), driverAction)
      .withRideId(ride);

    cascadedDriverAction
      .acceptRide()
      .reach()
      .startRide()
      .endRide(dropoffLocation.lat, dropoffLocation.lng);

    return ride;
  }
}
