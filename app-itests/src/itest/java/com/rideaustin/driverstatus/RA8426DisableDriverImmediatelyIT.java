
package com.rideaustin.driverstatus;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.model.Session;
import com.rideaustin.model.enums.DriverActivationStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Administrator;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.User;
import com.rideaustin.test.actions.AdministratorAction;
import com.rideaustin.test.asserts.DriverAssert;
import com.rideaustin.test.fixtures.AdministratorFixture;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;

@Category(DriverStatusManagement.class)
public class RA8426DisableDriverImmediatelyIT extends AbstractDriverStatusManagementTest {

  @Inject
  private ActiveDriverFixtureProvider activeDriverFixtureProvider;

  @Inject
  private AdministratorFixture administratorFixture;

  @Inject
  private AdministratorAction administratorAction;

  @Test
  public void testDisableDriverPermanently() throws Exception {
    ActiveDriver activeDriver = activeDriverFixtureProvider.create().getFixture();
    Administrator administrator = administratorFixture.getFixture();

    administratorAction.disableDriverPermanently(administrator.getEmail(), activeDriver.getDriver().getId());

    assertNoActiveSession(activeDriver.getDriver().getUser());
    assertDriverDisabled(activeDriver.getDriver().getId());

  }

  private void assertNoActiveSession(User user) {
    List<Session> sessionList = sessionDslRepository.findCurrentSessionsByUser(user.getId());
    assertThat(sessionList.size()).isEqualTo(0);
  }

  private void assertDriverDisabled(long driverId) {
    Driver driver = driverDslRepository.findById(driverId);
    DriverAssert.assertThat(driver)
      .isNotActive()
      .hasStatus(DriverActivationStatus.SUSPENDED);
  }
}
