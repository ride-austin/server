package com.rideaustin.rest;

import static org.junit.Assert.assertNull;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.test.AbstractNonTxTests;
import com.rideaustin.test.actions.RideAction;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.setup.RA13348Setup;
import com.rideaustin.test.util.TestUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public class RA13348DisablePreAuthorizationIT extends AbstractNonTxTests<RA13348Setup> {

  @Inject
  private RA13348Setup setup;

  @Inject
  private RideAction rideAction;

  @Inject
  private RideDslRepository repository;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    this.setup = setup.setUp();
  }

  @Test
  public void test() throws Exception {
    Long ride = rideAction.performRide(setup.getRider(), locationProvider.getCenter(), locationProvider.getOutsideAirportLocation(), setup.getActiveDriver(), TestUtils.HONDA);

    assertNull(repository.findOne(ride).getPreChargeId());
  }
}
