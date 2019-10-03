package com.rideaustin.driverstatistic.rest;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Random;

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
import com.rideaustin.driverstatistic.DriverStatisticTestConfig;
import com.rideaustin.model.user.Administrator;
import com.rideaustin.test.actions.AdministratorAction;
import com.rideaustin.test.common.ITestProfileSupport;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.fixtures.AdministratorFixture;
import com.rideaustin.testrail.TestCases;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class, DriverStatisticTestConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public class C1869781IT extends ITestProfileSupport {

  @Inject
  private AdministratorFixture administratorFixture;
  private Administrator administrator;

  @Inject
  private AdministratorAction administratorAction;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    administrator = administratorFixture.getFixture();
  }

  @Test
  @TestCases("C1869781")
  public void test() throws Exception {
    administratorAction.requestDriverStats(administrator.getEmail(), new Random().nextLong())
      .andExpect(status().isBadRequest());
  }
}
