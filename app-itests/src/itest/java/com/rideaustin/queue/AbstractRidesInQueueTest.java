package com.rideaustin.queue;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.google.maps.model.LatLng;
import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.model.Area;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.areaqueue.AreaQueueUpdateService;
import com.rideaustin.test.AbstractNonTxTests;
import com.rideaustin.test.actions.DriverAction;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.common.Sleeper;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.config.TestSetupConfig;
import com.rideaustin.test.setup.AbstractRidesInQueueSetup;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class, TestSetupConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public abstract class AbstractRidesInQueueTest<T extends AbstractRidesInQueueSetup<T>> extends AbstractNonTxTests<T> {

  @Inject
  protected RiderAction riderAction;

  @Inject
  private RideDslRepository rideRepository;

  @Inject
  protected AreaQueueUpdateService queueService;

  @Inject
  protected DriverAction driverAction;

  @Inject
  protected Sleeper sleeper;

  protected AbstractRidesInQueueSetup setup;

  private Area airport;

  protected Rider rider;
  protected ActiveDriver activeDriver;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    setup = createSetup();
    this.rider = setup.getRider();
    this.activeDriver = setup.getAvailableActiveDriver();
    this.airport = locationProvider.getAirport();
  }

  protected void requestAndDecline(ActiveDriver activeDriver, LatLng location) throws Exception {
    long rideId = riderAction
      .requestRide(rider.getEmail(), location);

    awaitDispatch(activeDriver, rideId);

    driverAction
      .declineRide(this.activeDriver, rideId)
      .andExpect(status().isOk());

    sleeper.sleep(3000);

    assertEquals(RideStatus.NO_AVAILABLE_DRIVER, rideRepository.findOne(rideId).getStatus());
  }

  protected void updateQueue() throws RideAustinException {
    queueService.updateStatuses(airport.getId());
  }
}
