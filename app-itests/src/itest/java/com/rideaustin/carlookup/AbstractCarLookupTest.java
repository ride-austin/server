package com.rideaustin.carlookup;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.actions.DriverAction;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.common.ITestProfileSupport;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.fixtures.RiderFixture;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;
import com.rideaustin.test.fixtures.providers.DriverFixtureProvider;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public abstract class AbstractCarLookupTest extends ITestProfileSupport {

  @Inject
  private RiderFixture riderFixture;
  protected Rider rider;

  @Inject
  protected ActiveDriverFixtureProvider activeDriverFixtureProvider;
  @Inject
  protected DriverFixtureProvider driverFixtureProvider;

  @Inject
  protected DriverAction driverAction;
  @Inject
  protected RiderAction riderAction;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    rider = riderFixture.getFixture();
  }
}
