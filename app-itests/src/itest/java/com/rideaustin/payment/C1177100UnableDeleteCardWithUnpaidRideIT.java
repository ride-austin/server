package com.rideaustin.payment;

import com.google.maps.model.LatLng;
import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.service.RiderCardService;
import com.rideaustin.test.AbstractNonTxTests;
import com.rideaustin.test.actions.DriverAction;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.config.TestSetupConfig;
import com.rideaustin.test.setup.C1177100Setup;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Category(Payment.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class, TestSetupConfig.class}, initializers = RAApplicationInitializer.class)
public class C1177100UnableDeleteCardWithUnpaidRideIT extends AbstractNonTxTests<C1177100Setup> {

  private LatLng defaultLocation;

  @Inject
  private RiderAction riderAction;
  @Inject
  private DriverAction driverAction;
  @Inject
  private RiderCardService riderCardService;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    setup = createSetup();
    defaultLocation = locationProvider.getCenter();
  }

  @Test
  @TestCases("C1177100")
  public void test() throws Exception {
    Rider rider = setup.getRider();
    ActiveDriver activeDriver = setup.getActiveDriver();

    driverAction.goOnline(activeDriver.getDriver().getEmail(), defaultLocation)
      .andExpect(status().isOk());

    Long rideId = riderAction.requestRide(rider.getEmail(), defaultLocation, TestUtils.REGULAR);

    riderCardService.lockCard(rider.getPrimaryCard(), rideDslRepository.findOne(rideId));
    riderAction.cancelRide(rider.getEmail(), rideId);
    awaitStatus(rideId, RideStatus.RIDER_CANCELLED);
    sleeper.sleep(5000);

    String errorContent = riderAction.deleteCard(rider.getEmail(), rider.getId(), rider.getPrimaryCard().getId())
      .andExpect(status().is4xxClientError())
      .andReturn()
      .getResponse()
      .getContentAsString();
    assertThat("Wrong content returned, should be 'Your credit card has been locked by your credit card company'.",
      errorContent.contains("Your credit card has been locked by your credit card company"));
  }
}
