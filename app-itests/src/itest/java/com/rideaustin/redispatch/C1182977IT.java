package com.rideaustin.redispatch;

import static org.junit.Assert.assertFalse;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
 * https://testrail.devfactory.com/index.php?/cases/view/1182977
 */
@Category(Redispatch.class)
public class C1182977IT extends AbstractRedispatchTest<DefaultRedispatchTestSetup> {

  @Test
  @TestCases("C1182977")
  public void test() throws Exception {
    doTestRedispatch(null);
  }

  @Override
  protected void goOnline(ActiveDriver firstDriver, ActiveDriver secondDriver, LatLng firstDriverLocation, LatLng secondDriverLocation) throws Exception {
    driverAction.locationUpdate(firstDriver, firstDriverLocation.lat, firstDriverLocation.lng)
      .andExpect(status().isOk());
  }

  @Override
  protected void cancelAndRedispatch(ActiveDriver firstDriver, ActiveDriver secondDriver, Long ride) throws Exception {
    driverAction.cancelRide(firstDriver.getDriver().getEmail(), ride)
      .andExpect(status().isOk());

    //assert that rider received no notifications
    assertFalse("Rider should not be notified about cancellation", notificationFacade.getDataMap().keySet().contains(RideStatus.DRIVER_CANCELLED.name()));
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
      .hasStartLocation(riderLocation)
      .hasEndLocation(destination)
      .hasStatus(RideStatus.NO_AVAILABLE_DRIVER);
  }
}
