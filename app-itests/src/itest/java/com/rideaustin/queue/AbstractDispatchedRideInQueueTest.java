package com.rideaustin.queue;

import javax.inject.Inject;

import org.junit.Before;
import org.springframework.test.context.ContextConfiguration;

import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.areaqueue.AreaQueueUpdateService;
import com.rideaustin.test.AbstractNonTxTests;
import com.rideaustin.test.actions.DriverAction;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.actions.cascaded.CascadedDriverAction;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.config.TestSetupConfig;
import com.rideaustin.test.setup.BaseDispatchedRideInQueueSetup;

@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class, TestSetupConfig.class}, initializers = RAApplicationInitializer.class)
public abstract class AbstractDispatchedRideInQueueTest extends AbstractNonTxTests<BaseDispatchedRideInQueueSetup> {

  @Inject
  protected RiderAction riderAction;

  @Inject
  protected DriverAction driverAction;
  @Inject
  protected AreaQueueUpdateService queueService;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    setup = createSetup();
  }

  protected void acceptAndStartRide(ActiveDriver activeDriver, long rideId) throws Exception {
    CascadedDriverAction cascadedDriverAction = new CascadedDriverAction(activeDriver.getDriver(), driverAction);
    cascadedDriverAction
      .withRideId(rideId)
      .acceptRide()
      .reach()
      .startRide();
    updateQueue();
  }

  protected void updateQueue() throws RideAustinException {
    queueService.updateStatuses(locationProvider.getAirport().getId());
  }

}
