package com.rideaustin.driverstatus;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.actions.cascaded.CascadedDriverAction;
import com.rideaustin.test.setup.C1177093Setup;
import com.rideaustin.testrail.TestCases;

@Category(DriverStatusManagement.class)
public class C1177091IT extends AbstractNonTxDriverStatusManagementTest<C1177093Setup> {

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    setup = createSetup();
  }

  @Test
  @TestCases("C1177091")
  public void shouldNotGoOffline_WhenMissedRidesLessThanLimit() throws Exception {
    ActiveDriver activeDriver = setup.getActiveDriver();
    CascadedDriverAction cascadedAction = new CascadedDriverAction(activeDriver.getDriver(), driverAction);
    cascadedAction
      .goOnline(austinCenter.lat, austinCenter.lng)
      .locationUpdate(activeDriver, austinCenter.lat, austinCenter.lng);

    Rider rider = setup.getRider();
    for (int i = 0; i < declinedRidesLimit - 1; i++) {
      requestAndDecline(activeDriver, cascadedAction, rider);
    }

    ActiveDriver updatedActiveDriver = activeDriverDslRepository.findById(activeDriver.getId());
    assertThat(updatedActiveDriver.getStatus()).isEqualTo(ActiveDriverStatus.AVAILABLE);
  }

  private void requestAndDecline(ActiveDriver activeDriver, CascadedDriverAction cascadedAction, Rider rider) throws Exception {
    Long ride = riderAction.requestRide(rider.getEmail(), austinCenter);
    awaitDispatch(activeDriver, ride);
    cascadedAction
      .withRideId(ride)
      .declineRide();
    sleeper.sleep(10L);
  }

  @Test
  @TestCases("C1177091")
  public void shouldGoOffline_WhenMissedRidesGreaterThanLimit() throws Exception {
    ActiveDriver activeDriver = setup.getActiveDriver();
    CascadedDriverAction cascadedAction = new CascadedDriverAction(activeDriver.getDriver(), driverAction);
    cascadedAction
      .goOnline(austinCenter.lat, austinCenter.lng)
      .locationUpdate(activeDriver, austinCenter.lat, austinCenter.lng);

    Rider rider = setup.getRider();
    for (int i = 0; i < declinedRidesLimit; i++) {
      requestAndDecline(activeDriver, cascadedAction, rider);
    }

    ActiveDriver updatedActiveDriver = activeDriverDslRepository.findById(activeDriver.getId());
    assertThat(updatedActiveDriver.getStatus()).isEqualTo(ActiveDriverStatus.INACTIVE);

  }
}
