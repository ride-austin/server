package com.rideaustin.dispatch.womenonly;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

import com.rideaustin.model.Area;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.DriverType;
import com.rideaustin.service.areaqueue.AreaQueueUpdateService;
import com.rideaustin.test.util.TestUtils;

public class WOInAirportQueueIT extends AbstractWomenOnlyDispatchTest {

  @Inject
  private AreaQueueUpdateService queueService;
  private Area airport;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    airport = locationProvider.getAirport();
  }

  @Test
  public void test() throws Exception {
    driverAction.goOnline(regularDriver.getDriver().getEmail(), locationProvider.getAirportLocation());
    queueService.updateStatuses(airport.getId());

    driverAction.goOnline(woFPDriver.getDriver().getEmail(), locationProvider.getAirportLocation());
    queueService.updateStatuses(airport.getId());

    final Long ride = riderAction.requestRide(rider.getEmail(), locationProvider.getAirportLocation(), TestUtils.REGULAR,
      new String[]{DriverType.WOMEN_ONLY, DriverType.FINGERPRINTED});

    awaitDispatch(woFPDriver, ride);
    driverAction.declineRide(woFPDriver.getDriver().getEmail(), ride);
    awaitStatus(ride, RideStatus.NO_AVAILABLE_DRIVER);

    driverAction.goOffline(regularDriver.getDriver().getEmail());
    driverAction.goOffline(woFPDriver.getDriver().getEmail());
    queueService.updateStatuses(airport.getId());
  }

  @Test
  public void testA() throws Exception {
    driverAction.goOnline(regularDriver.getDriver().getEmail(), locationProvider.getAirportLocation());
    queueService.updateStatuses(airport.getId());

    driverAction.goOnline(woFPDriver.getDriver().getEmail(), locationProvider.getAirportLocation());
    queueService.updateStatuses(airport.getId());

    final Long ride = riderAction.requestRide(rider.getEmail(), locationProvider.getAirportLocation(), TestUtils.REGULAR,
      DriverType.WOMEN_ONLY);

    awaitDispatch(woFPDriver, ride);
    driverAction.declineRide(woFPDriver.getDriver().getEmail(), ride);
    awaitStatus(ride, RideStatus.NO_AVAILABLE_DRIVER);

    driverAction.goOffline(regularDriver.getDriver().getEmail());
    driverAction.goOffline(woFPDriver.getDriver().getEmail());
    queueService.updateStatuses(airport.getId());
  }

  @Test
  public void testB() throws Exception {
    driverAction.goOnline(regularDriver.getDriver().getEmail(), locationProvider.getAirportLocation());
    queueService.updateStatuses(airport.getId());

    driverAction.goOnline(woFPDriver.getDriver().getEmail(), locationProvider.getAirportLocation());
    queueService.updateStatuses(airport.getId());

    final Long ride = riderAction.requestRide(rider.getEmail(), locationProvider.getAirportLocation(), TestUtils.REGULAR,
      DriverType.FINGERPRINTED);

    awaitDispatch(regularDriver, ride);
    driverAction.declineRide(regularDriver.getDriver().getEmail(), ride);
    awaitDispatch(woFPDriver, ride);
    driverAction.declineRide(woFPDriver.getDriver().getEmail(), ride);
    awaitStatus(ride, RideStatus.NO_AVAILABLE_DRIVER);

    driverAction.goOffline(regularDriver.getDriver().getEmail());
    driverAction.goOffline(woFPDriver.getDriver().getEmail());
    queueService.updateStatuses(airport.getId());
  }
}
