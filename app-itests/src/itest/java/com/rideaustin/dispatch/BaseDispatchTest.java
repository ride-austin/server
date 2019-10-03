package com.rideaustin.dispatch;

import javax.inject.Inject;

import org.junit.Before;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;

import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.test.AbstractNonTxTests;
import com.rideaustin.test.actions.DriverAction;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.config.TestSetupConfig;
import com.rideaustin.test.setup.SetupAction;

@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class, TestSetupConfig.class}, initializers = RAApplicationInitializer.class)
public abstract class BaseDispatchTest<T extends SetupAction<T>> extends AbstractNonTxTests<T> {

  @Inject
  protected Environment environment;

  @Inject
  protected DriverAction driverAction;

  @Inject
  protected RiderAction riderAction;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    this.setup = createSetup();
  }
}
