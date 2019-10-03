package com.rideaustin.driverstatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.service.ActiveDriverLocationService;
import com.rideaustin.service.location.enums.LocationType;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.test.actions.cascaded.CascadedDriverAction;
import com.rideaustin.test.setup.C1177093Setup;
import com.rideaustin.testrail.TestCases;

@Category(DriverStatusManagement.class)
public class C1177092IT extends AbstractNonTxDriverStatusManagementTest<C1177093Setup> {

  @Inject
  private ActiveDriverLocationService activeDriverLocationService;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    setup = createSetup();
  }

  @Test
  @TestCases("C1177092")
  public void shouldNotDisableCarCategory_WhenMissedRidesIsLessThanLimit() throws Exception {
    ActiveDriver activeDriver = setup.getActiveDriver();
    CascadedDriverAction cascadedAction = new CascadedDriverAction(activeDriver.getDriver(), driverAction);
    cascadedAction
      .goOnline(austinCenter.lat, austinCenter.lng)
      .locationUpdate(activeDriver, austinCenter.lat, austinCenter.lng);

    Rider rider = setup.getRider();
    for (int i = 0; i < declinedRidesLimit - 1; i++) {
      requestAndDecline(activeDriver, cascadedAction, rider);
    }

    OnlineDriverDto updatedActiveDriver = activeDriverLocationService.getById(activeDriver.getId(), LocationType.ACTIVE_DRIVER);
    assertEquals((Integer)1, updatedActiveDriver.getAvailableCarCategoriesBitmask());
  }

  @Test
  @TestCases("C1177092")
  public void shouldDisableCarCategory_WhenMissedRidesIsGreaterThanLimit() throws Exception {
    ActiveDriver activeDriver = setup.getActiveDriver();
    CascadedDriverAction cascadedAction = new CascadedDriverAction(activeDriver.getDriver(), driverAction);
    cascadedAction
      .goOnline(austinCenter.lat, austinCenter.lng)
      .locationUpdate(activeDriver, austinCenter.lat, austinCenter.lng);

    Rider rider = setup.getRider();
    for (int i = 0; i < declinedRidesLimit; i++) {
      requestAndDecline(activeDriver, cascadedAction, rider);
    }

    OnlineDriverDto updatedActiveDriver = activeDriverLocationService.getById(activeDriver.getId(), LocationType.ACTIVE_DRIVER);
    assertNull(updatedActiveDriver);
  }

  private void requestAndDecline(ActiveDriver activeDriver, CascadedDriverAction cascadedAction, Rider rider) throws Exception {
    Long ride = riderAction.requestRide(rider.getEmail(), austinCenter);
    awaitDispatch(activeDriver, ride);
    cascadedAction
      .withRideId(ride)
      .declineRide();
    sleeper.sleep(10L);
  }
}
