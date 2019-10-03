package com.rideaustin.dispatch.actions;

import org.springframework.statemachine.StateContext;

import com.rideaustin.dispatch.aop.DeferredResultAction;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DriverCancelAction extends BaseCancelAction {

  @Override
  @DeferredResultAction
  public void execute(StateContext<States, Events> context) {
    super.execute(context);
  }

  @Override
  protected RideStatus getStatus() {
    return RideStatus.DRIVER_CANCELLED;
  }

  @Override
  protected void notifyRider(long id, StateContext<States, Events> context) {
    if (context.getTransition().getSource().getId() != States.DRIVER_ASSIGNED) {
      pushNotificationsFacade.sendRideUpdateToRider(id, RideStatus.DRIVER_CANCELLED);
    }
  }

  @Override
  protected boolean isInSingleRide(long activeDriverId) {
    return rideDslRepository.findNextRide(activeDriverId) == null;
  }
}
