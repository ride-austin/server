package com.rideaustin.driverstatistic.rest;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.rideaustin.driverstatistic.model.DriverStatistic;
import com.rideaustin.model.user.Driver;
import com.rideaustin.test.actions.DriverAction;
import com.rideaustin.test.common.ITestProfileSupport;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.fixtures.providers.DriverFixtureProvider;
import com.rideaustin.test.fixtures.providers.DriverStatisticFixtureProvider;
import com.rideaustin.testrail.TestCases;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class, DriverStatisticTestConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public class C1869782IT extends ITestProfileSupport {

  @Inject
  private DriverStatisticFixtureProvider provider;

  @Inject
  private DriverAction driverAction;

  @Inject
  private DriverFixtureProvider driverFixtureProvider;
  private Driver driver;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    this.driver = driverFixtureProvider.create().getFixture();
  }

  @Test
  @TestCases("C1869782")
  public void test() throws Exception {
    DriverStatistic statistic = provider.create(driver.getId()).getFixture();
    driverAction.requestDriverStats(driver)
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.[0].value").value(0))
      .andExpect(jsonPath("$.[1].value").value(0))
      .andDo(print());
  }
}
