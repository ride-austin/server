package com.rideaustin.dispatch.guards;

import javax.inject.Inject;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;

import com.rideaustin.service.config.StackedRidesConfig;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.RideRequestContext;
import com.rideaustin.utils.dispatch.StateMachineUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ForceRedispatchGuard implements Guard<States, Events> {

  @Inject
  private StackedRidesConfig config;

  @Override
  public boolean evaluate(StateContext<States, Events> context) {
    RideRequestContext requestContext = StateMachineUtils.getRequestContext(context);
    return config.isForceRedispatchEnabled(requestContext.getCityId());
  }
}
