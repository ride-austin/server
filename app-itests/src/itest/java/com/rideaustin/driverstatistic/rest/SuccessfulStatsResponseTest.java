package com.rideaustin.driverstatistic.rest;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.inject.Inject;

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
import com.rideaustin.test.fixtures.providers.DriverStatisticFixtureProvider;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class, DriverStatisticTestConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public abstract class SuccessfulStatsResponseTest extends ITestProfileSupport {

  @Inject
  private DriverStatisticFixtureProvider provider;

  @Inject
  private DriverAction driverAction;

  protected void doTest() throws Exception {
    int over = 100;
    int lastAccepted = 50;
    int lastCancelled = 2;
    DriverStatistic statistic = provider.create(getDriver().getId(), lastAccepted, over, lastCancelled, over).getFixture();
    logger.info(statistic.getId());
    driverAction.requestDriverStats(getDriver())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.[0].value").value(lastAccepted))
      .andExpect(jsonPath("$.[1].value").value(lastCancelled))
      .andDo(print());
  }

  protected abstract Driver getDriver();

}
