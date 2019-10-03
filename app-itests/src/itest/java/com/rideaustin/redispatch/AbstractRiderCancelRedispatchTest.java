package com.rideaustin.redispatch;

import javax.inject.Inject;

import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.service.model.States;
import com.rideaustin.test.common.Sleeper;
import com.rideaustin.test.setup.DefaultRedispatchTestSetup;

public abstract class AbstractRiderCancelRedispatchTest extends AbstractRedispatchTest<DefaultRedispatchTestSetup> {

  @Inject
  protected Sleeper sleeper;

  @Override
  protected void acceptRedispatched(ActiveDriver driver, Long ride) throws Exception {
    super.acceptRedispatched(driver, ride);
    waitForCancellation();
    riderAction.cancelRide(rider.getEmail(), ride);

    awaitState(ride, States.RIDER_CANCELLED, States.ENDED);
    awaitStatus(ride, RideStatus.RIDER_CANCELLED);
  }

  protected void waitForCancellation() {
    //do nothing
  }
}
