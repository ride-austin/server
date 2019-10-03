package com.rideaustin.dispatch.guards;

import org.springframework.statemachine.StateContext;

import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.utils.dispatch.StateMachineUtils;

public class AuthorizedDriverGuard extends BaseAuthorizedGuard {
  @Override
  protected boolean doEvaluate(StateContext<States, Events> context) {
    return rideOwnerService.isDriversRide(StateMachineUtils.getRideId(context));
  }
}
