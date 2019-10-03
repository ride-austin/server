package com.rideaustin.payment;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.model.user.Rider;
import com.rideaustin.service.RiderCardService;
import com.rideaustin.test.AbstractNonTxTests;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.ITestProfile;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.setup.C117709Setup;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

@ITestProfile
@WebAppConfiguration
@Category(Payment.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class}, initializers = RAApplicationInitializer.class)
public class C117709UnableRequestIfUnpaidRideForPrimaryCardIT extends AbstractNonTxTests<C117709Setup> {

  @Inject
  private RiderAction riderAction;
  @Inject
  private RiderCardService riderCardService;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    setup = setup.setUp();
  }

  @Test
  @TestCases("C1177099")
  public void test() throws Exception {

    Rider rider = setup.getRider();
    Long rideId = riderAction.requestRide(rider.getEmail(), locationProvider.getRandomLocation(), TestUtils.REGULAR, null, null);

    riderCardService.lockCard(rider.getPrimaryCard(), rideDslRepository.findOne(rideId));
    sleeper.sleep(5000);

    String errorContent = riderAction.performRideRequest(rider.getEmail(), locationProvider.getRandomLocation(), TestUtils.REGULAR)
      .andExpect(status().is4xxClientError())
      .andReturn()
      .getResponse()
      .getContentAsString();
    assertThat("Wrong content returned, should be 'Your credit card has been locked by your credit card company'.",
      errorContent.contains("Your credit card has been locked by your credit card company"));
  }
}
