package com.rideaustin.payment;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import com.google.maps.model.LatLng;
import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.RiderCard;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.RiderCardService;
import com.rideaustin.test.AbstractNonTxTests;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.config.TestSetupConfig;
import com.rideaustin.test.setup.C1177101Setup;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

@Category(Payment.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class, TestSetupConfig.class}, initializers = RAApplicationInitializer.class)
public class C1177101UnableSetAsPrimaryWithUnpaidRideOnItIT extends AbstractNonTxTests<C1177101Setup> {

  private final LatLng defaultLocation = new LatLng(30.269372, -97.740394);

  @Inject
  private CurrentUserService currentUserService;
  @Inject
  private RideDslRepository rideDslRepository;
  @Inject
  private RiderCardService riderCardService;
  @Inject
  private RiderAction riderAction;

  private C1177101Setup setup;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    setup = createSetup();
  }

  @Test
  @TestCases("C1177101")
  public void test() throws Exception {

    Rider rider = setup.getRider();

    String secondCardToken = "asdqwesdfsfdg234";
    currentUserService.setUser(rider.getUser());
    RiderCard newCard = riderCardService.addRiderCard(rider.getId(), secondCardToken);

    Long rideId = riderAction.requestRide(rider.getEmail(), defaultLocation, TestUtils.REGULAR);

    riderCardService.lockCard(newCard, rideDslRepository.findOne(rideId));

    String errorContent = riderAction.updateCard(rider.getEmail(), rider.getId(), newCard.getId())
      .andExpect(status().is4xxClientError())
      .andReturn()
      .getResponse()
      .getContentAsString();
    assertThat("Wrong content returned, should be 'Your credit card has been locked by your credit card company'.",
      errorContent.contains("Your credit card has been locked by your credit card company"));
  }
}
