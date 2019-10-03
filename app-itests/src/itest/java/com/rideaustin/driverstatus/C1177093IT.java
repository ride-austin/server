package com.rideaustin.driverstatus;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.test.response.AreaQueuePositions;
import com.rideaustin.service.ActiveDriverLocationService;
import com.rideaustin.service.location.enums.LocationType;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.test.actions.cascaded.CascadedDriverAction;
import com.rideaustin.test.asserts.DriverQueueAssert;
import com.rideaustin.test.setup.C1177093Setup;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

@Category(DriverStatusManagement.class)
public class C1177093IT extends AbstractNonTxDriverStatusManagementTest<C1177093Setup> {

  @Inject
  private ActiveDriverLocationService activeDriverLocationService;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    setup = createSetup();
  }

  @Test
  @TestCases("C1177093")
  public void shouldNotGoOffline_WhenInQueue_DecliningOutsideRequests() throws Exception {
    ActiveDriver activeDriver = setup.getActiveDriver();
    CascadedDriverAction cascadedAction = new CascadedDriverAction(activeDriver.getDriver(), driverAction);
    cascadedAction
      .goOnline(airportLocation.lat, airportLocation.lng)
      .locationUpdate(activeDriver, airportLocation.lat, airportLocation.lng);
    updateQueue();
    AreaQueuePositions queuePosition = driverAction.getQueuePosition(activeDriver.getDriver());
    DriverQueueAssert.assertThat(queuePosition)
      .hasPosition(TestUtils.REGULAR, 0)
      .hasLength(TestUtils.REGULAR, 1);

    Rider rider = setup.getRider();
    for (int i = 0; i < declinedRidesLimit; i++) {
      Long ride = riderAction.requestRide(rider.getEmail(), austinCenter);
      awaitDispatch(activeDriver, ride);
      cascadedAction
        .withRideId(ride)
        .declineRide();
      sleeper.sleep(10);
    }

    OnlineDriverDto updatedActiveDriver = activeDriverLocationService.getById(activeDriver.getId(), LocationType.ACTIVE_DRIVER);

    assertThat(updatedActiveDriver.getStatus()).isEqualTo(ActiveDriverStatus.AVAILABLE);
    assertThat(updatedActiveDriver.getAvailableCarCategoriesBitmask()).isEqualTo(1);

    queuePosition = driverAction.getQueuePosition(activeDriver.getDriver());
    DriverQueueAssert.assertThat(queuePosition)
      .hasPosition(TestUtils.REGULAR, 0)
      .hasLength(TestUtils.REGULAR, 1);
  }

  protected void updateQueue() throws RideAustinException {
    queueService.updateStatuses(airport.getId());
  }
}