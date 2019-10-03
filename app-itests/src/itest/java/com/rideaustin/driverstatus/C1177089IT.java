package com.rideaustin.driverstatus;

import com.rideaustin.jobs.ActiveDriverDeactivateJob;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.test.actions.cascaded.CascadedDriverAction;
import com.rideaustin.test.setup.C1177093Setup;
import com.rideaustin.testrail.TestCases;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@Category(DriverStatusManagement.class)
@DeactivationJobFrequency
public class C1177089IT extends AbstractNonTxDriverStatusManagementTest<C1177093Setup> {

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
    setup = createSetup();
  }

  @Test
  @TestCases("C1177089")
  public void shouldGoAway_WhenNotInRide() throws Exception {
    ActiveDriver activeDriver = setup.getActiveDriver();
    CascadedDriverAction cascadedAction = new CascadedDriverAction(activeDriver.getDriver(), driverAction);

    cascadedAction
      .goOnline(austinCenter.lat, austinCenter.lng)
      .locationUpdate(activeDriver, austinCenter.lat, austinCenter.lng);

    sleeper.sleep(2000);

    schedulerService.triggerJob(ActiveDriverDeactivateJob.class, Collections.emptyMap());

    assertThat(activeDriverDslRepository.findById(activeDriver.getId()).getStatus()).isEqualTo(ActiveDriverStatus.AWAY);
  }

  @Test
  @TestCases("C1177089")
  public void shouldGoInactive_WhenNotInRide() throws Exception {
    ActiveDriver activeDriver = setup.getActiveDriver();
    CascadedDriverAction cascadedAction = new CascadedDriverAction(activeDriver.getDriver(), driverAction);

    cascadedAction
      .goOnline(austinCenter.lat, austinCenter.lng)
      .locationUpdate(activeDriver, austinCenter.lat, austinCenter.lng);

    sleeper.sleep(4000);

    schedulerService.triggerJob(ActiveDriverDeactivateJob.class, Collections.emptyMap());

    assertThat(activeDriverDslRepository.findById(activeDriver.getId()).getStatus()).isEqualTo(ActiveDriverStatus.INACTIVE);
  }
}
