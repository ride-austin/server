package com.rideaustin.driverstatus;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.jobs.ActiveDriverDeactivateJob;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.actions.cascaded.CascadedDriverAction;
import com.rideaustin.test.setup.C1177093Setup;
import com.rideaustin.testrail.TestCases;

@Category(DriverStatusManagement.class)
@DeactivationJobFrequency
public class C1177090IT extends AbstractNonTxDriverStatusManagementTest<C1177093Setup> {

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    setup = createSetup();
  }

  @Test
  @TestCases("C1177090")
  public void shouldNotGoAway_WhenInRide() throws Exception {
    Rider rider = setup.getRider();
    ActiveDriver activeDriver = setup.getActiveDriver();
    CascadedDriverAction cascadedAction = new CascadedDriverAction(activeDriver.getDriver(), driverAction);

    cascadedAction
      .goOnline(austinCenter.lat, austinCenter.lng)
      .locationUpdate(activeDriver, austinCenter.lat, austinCenter.lng);

    Long rideId = riderAction.requestRide(rider.getEmail(), austinCenter);
    awaitDispatch(activeDriver, rideId);
    cascadedAction
      .withRideId(rideId)
      .acceptRide()
      .reach()
      .startRide();

    sleeper.sleep(2000L);

    schedulerService.triggerJob(ActiveDriverDeactivateJob.class, Collections.emptyMap());

    ActiveDriver updatedActiveDriver = activeDriverDslRepository.findById(activeDriver.getId());
    assertThat(updatedActiveDriver.getStatus()).isEqualTo(ActiveDriverStatus.RIDING);
  }

  @Test
  @TestCases("C1177090")
  public void shouldNotGoInactive_WhenInRide() throws Exception {
    Rider rider = setup.getRider();
    ActiveDriver activeDriver = setup.getActiveDriver();
    CascadedDriverAction cascadedAction = new CascadedDriverAction(activeDriver.getDriver(), driverAction);

    cascadedAction
      .goOnline(austinCenter.lat, austinCenter.lng)
      .locationUpdate(activeDriver, austinCenter.lat, austinCenter.lng);

    Long rideId = riderAction.requestRide(rider.getEmail(), austinCenter);
    awaitDispatch(activeDriver, rideId);
    cascadedAction
      .withRideId(rideId)
      .acceptRide()
      .reach()
      .startRide();

    sleeper.sleep(4000);

    schedulerService.triggerJob(ActiveDriverDeactivateJob.class, Collections.emptyMap());

    ActiveDriver updatedActiveDriver = activeDriverDslRepository.findById(activeDriver.getId());
    assertThat(updatedActiveDriver.getStatus()).isEqualTo(ActiveDriverStatus.RIDING);
  }
}
