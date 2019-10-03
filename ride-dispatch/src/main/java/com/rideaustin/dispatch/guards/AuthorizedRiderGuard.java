package com.rideaustin.dispatch.guards;

import org.springframework.statemachine.StateContext;

import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.utils.dispatch.StateMachineUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthorizedRiderGuard extends BaseAuthorizedGuard {

  protected boolean doEvaluate(StateContext<States, Events> context) {
    return rideOwnerService.isRideRider(StateMachineUtils.getRideId(context));
  }
}
