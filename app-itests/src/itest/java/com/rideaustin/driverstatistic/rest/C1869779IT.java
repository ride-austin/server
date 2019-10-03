package com.rideaustin.driverstatistic.rest;

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
import com.rideaustin.model.user.Driver;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.fixtures.providers.DriverFixtureProvider;
import com.rideaustin.testrail.TestCases;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class, DriverStatisticTestConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public class C1869779IT extends SuccessfulStatsResponseTest {

  @Inject
  private DriverFixtureProvider driverFixtureProvider;
  private Driver driver;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    this.driver = driverFixtureProvider.create().getFixture();
  }

  @Override
  protected Driver getDriver() {
    return driver;
  }

  @Test
  @TestCases("C1869779")
  public void test() throws Exception {
    doTest();
  }

}
