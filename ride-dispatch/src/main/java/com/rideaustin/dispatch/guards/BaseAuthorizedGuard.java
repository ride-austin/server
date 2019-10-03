package com.rideaustin.dispatch.guards;

import static com.rideaustin.dispatch.LogUtil.flowError;

import javax.inject.Inject;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;

import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.ride.RideOwnerService;
import com.rideaustin.utils.dispatch.StateMachineUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseAuthorizedGuard implements Guard<States, Events> {

  @Inject
  protected RideOwnerService rideOwnerService;

  @Override
  public boolean evaluate(StateContext<States, Events> context) {
    try {
      return doEvaluate(context);
    } catch (Exception e) {
      flowError(log, StateMachineUtils.getRequestContext(context), String.format("Error occurred in %s", getClass().getCanonicalName()), e);
    }
    return false;
  }

  protected abstract boolean doEvaluate(StateContext<States, Events> context);
}
