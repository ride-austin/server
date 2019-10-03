package com.rideaustin.dispatch;

import java.util.Optional;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.statemachine.StateMachineContext;

import com.jayway.awaitility.Awaitility;
import com.rideaustin.redispatch.Redispatch;
import com.rideaustin.service.model.States;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;
import com.rideaustin.utils.dispatch.StateMachineUtils;

@Category(Redispatch.class)
public class C1177071DispatchToNextAfterFirstDeclineIT extends BaseC1177071Test {

  @Test
  @TestCases("C1177071")
  public void test() throws Exception {

    driverAction.goOnline(firstActiveDriver.getDriver().getEmail(), defaultClosestLocation.lat, defaultClosestLocation.lng);
    driverAction.goOnline(secondActiveDriver.getDriver().getEmail(), defaultNotClosestLocation.lat, defaultNotClosestLocation.lng);

    Long rideId = riderAction.requestRide(rider.getEmail(), defaultClosestLocation, TestUtils.REGULAR);

    Awaitility.await().until(() -> Optional.ofNullable(StateMachineUtils.getPersistedContext(environment, contextAccess, rideId))
      .map(StateMachineContext::getState)
      .map(States.DISPATCH_PENDING::equals)
      .orElse(false));

    driverAction.declineRide(firstActiveDriver.getDriver().getEmail(), rideId);

    awaitDispatch(secondActiveDriver, rideId);

  }

}
