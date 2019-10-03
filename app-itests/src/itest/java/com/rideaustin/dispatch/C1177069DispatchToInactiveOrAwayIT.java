package com.rideaustin.dispatch;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.statemachine.StateMachineContext;

import com.google.maps.model.LatLng;
import com.jayway.awaitility.Awaitility;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.redispatch.Redispatch;
import com.rideaustin.repo.dsl.ActiveDriverDslRepository;
import com.rideaustin.service.model.States;
import com.rideaustin.test.setup.DefaultRedispatchTestSetup;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;
import com.rideaustin.utils.dispatch.StateMachineUtils;

@Category(Redispatch.class)
public class C1177069DispatchToInactiveOrAwayIT extends BaseDispatchTest<DefaultRedispatchTestSetup> {

  private LatLng defaultClosestLocation = new LatLng(30.269372, -97.740394);

  @Inject
  private ActiveDriverDslRepository activeDriverDslRepository;

  private Rider rider;
  private ActiveDriver firstActiveDriver;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    rider = this.setup.getRider();
    firstActiveDriver = this.setup.getFirstActiveDriver();
  }

  @Test
  @TestCases("C1177069")
  public void test() throws Exception {

    driverAction.locationUpdate(firstActiveDriver, defaultClosestLocation.lat, defaultClosestLocation.lng);

    updateActiveDriverStatus(firstActiveDriver.getId(), ActiveDriverStatus.AWAY);

    assertActiveDriverStatus(firstActiveDriver.getId(), ActiveDriverStatus.AWAY);

    Long rideId = riderAction.requestRide(rider.getEmail(), defaultClosestLocation, TestUtils.REGULAR);

    Awaitility.await()
      .atMost(90, TimeUnit.SECONDS)
      .until(() ->
        Optional.ofNullable(StateMachineUtils.getPersistedContext(environment, contextAccess, rideId))
          .map(StateMachineContext::getState)
          .map(States.NO_AVAILABLE_DRIVER::equals)
          .orElse(false));

  }

  private void updateActiveDriverStatus(Long activeDriverId, ActiveDriverStatus status) {
    ActiveDriver activeDriver = activeDriverDslRepository.findById(activeDriverId);
    activeDriver.setStatus(status);
    activeDriverDslRepository.save(activeDriver);
  }

  private void assertActiveDriverStatus(Long activeDriverId, ActiveDriverStatus status) throws Exception {
    ActiveDriver activeDriver = activeDriverDslRepository.findById(activeDriverId);
    assertThat("", activeDriver.getStatus().equals(status));
  }
}
