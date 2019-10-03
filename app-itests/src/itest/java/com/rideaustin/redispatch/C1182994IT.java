package com.rideaustin.redispatch;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.statemachine.StateMachineContext;

import com.google.maps.model.LatLng;
import com.jayway.awaitility.Awaitility;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.rest.model.MobileRiderRideDto;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.test.asserts.RiderRideAssert;
import com.rideaustin.test.setup.DefaultRedispatchTestSetup;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;
import com.rideaustin.utils.dispatch.StateMachineUtils;

/**
 * https://testrail.devfactory.com/index.php?/cases/view/1182994
 */
@Category(Redispatch.class)
public class C1182994IT extends AbstractRedispatchTest<DefaultRedispatchTestSetup> {

  @Test
  @TestCases("C1182994")
  public void test() throws Exception {
    doTestRedispatch(null);
  }

  @Override
  protected void acceptRedispatched(ActiveDriver driver, Long ride) throws Exception {
    super.acceptRedispatched(driver, ride);
    driverAction.cancelRide(driver.getDriver().getEmail(), ride);
  }

  @Override
  protected void assertRideRedispatched(LatLng destination, ActiveDriver secondDriver, LatLng riderLocation, LatLng secondDriverLocation, Long ride) throws Exception {
    Awaitility.await()
      .atMost(dispatchTimeout, TimeUnit.SECONDS)
      .pollInterval(10, TimeUnit.MILLISECONDS)
      .until(() -> {
        Optional<StateMachineContext<States, Events>> persistedContext = Optional.ofNullable(StateMachineUtils.getPersistedContext(environment, contextAccess, ride));
        return persistedContext.map(StateMachineContext::getState)
          .map(States.NO_AVAILABLE_DRIVER::equals)
          .orElse(false);
      });

    MobileRiderRideDto rideInfo = riderAction.getRideInfo(rider.getEmail(), ride);

    RiderRideAssert.assertThat(rideInfo)
      .hasRequestedCategory(TestUtils.REGULAR)
      .hasStatus(RideStatus.NO_AVAILABLE_DRIVER)
      .hasDriverAssigned(null)
      .hasStartLocation(riderLocation)
      .hasEndLocation(destination);
  }
}
