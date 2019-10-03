package com.rideaustin.dispatch.stacked;

import static org.junit.Assert.assertEquals;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.google.maps.model.LatLng;
import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.RideDriverDispatch;
import com.rideaustin.model.user.Rider;
import com.rideaustin.repo.dsl.RideDriverDispatchDslRepository;
import com.rideaustin.test.AbstractNonTxTests;
import com.rideaustin.test.actions.DriverAction;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.actions.cascaded.CascadedDriverAction;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.setup.RA14913Setup;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public class RA14913IT extends AbstractNonTxTests<RA14913Setup> {

  @Inject
  private RiderAction riderAction;
  @Inject
  private DriverAction driverAction;

  @Inject
  private RideDriverDispatchDslRepository repository;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    setup = createSetup();
    configurationItemCache.setBooleanConfig(1L, ClientType.CONSOLE, "stackedRides", "enabled", true);
  }

  @Test
  public void test() throws Exception {
    ActiveDriver activeDriver = setup.getActiveDriver();
    LatLng center = locationProvider.getCenter();
    driverAction.goOnline(activeDriver.getDriver().getEmail(), center);

    Rider firstRider = setup.getRiders().get(0);
    Long ride = riderAction.requestRide(firstRider.getEmail(), center, center);
    awaitDispatch(activeDriver, ride);
    CascadedDriverAction cascadedDriverAction = new CascadedDriverAction(activeDriver.getDriver(), driverAction)
      .withRideId(ride);

    cascadedDriverAction.acceptRide()
      .reach()
      .startRide();

    driverAction.locationUpdate(activeDriver, center.lat, center.lng);

    Long stackedRequest = ride;
    for (int i = 1; i < 10; i++) {
      Rider rider = setup.getRiders().get(i);
      stackedRequest = riderAction.requestRide(rider.getEmail(), center);

      if (i == 1) {
        awaitDispatch(activeDriver, stackedRequest);
        cascadedDriverAction.withRideId(stackedRequest)
          .acceptRide();
      }
    }

    awaitStatus(stackedRequest, RideStatus.NO_AVAILABLE_DRIVER);

    int dispatched = 0;
    for (int i = 0; i < 10; i++) {
      long id = i + 1L;
      RideDriverDispatch rdd = repository.findByRideAndActiveDriver(id, activeDriver.getId());
      dispatched += rdd == null ? 0 : 1;
      logger.info("ride " + id + ": RDD present " + (rdd != null));
    }

    assertEquals(2, dispatched);
  }
}
