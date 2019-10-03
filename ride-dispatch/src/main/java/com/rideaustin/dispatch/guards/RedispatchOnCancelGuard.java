package com.rideaustin.dispatch.guards;

import static com.rideaustin.dispatch.LogUtil.dispatchInfo;

import javax.inject.Inject;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;

import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.filter.ClientType;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.service.model.context.RideRequestContext;
import com.rideaustin.utils.dispatch.StateMachineUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RedispatchOnCancelGuard implements Guard<States, Events> {

  private static final String REDISPATCH_ON_CANCEL_CONFIG_KEY = "redispatchOnCancel";

  @Inject
  private ConfigurationItemCache configurationCache;

  @Override
  public boolean evaluate(StateContext<States, Events> context) {
    DispatchContext dispatchContext = StateMachineUtils.getDispatchContext(context);
    RideRequestContext requestContext = StateMachineUtils.getRequestContext(context);

    dispatchInfo(log, requestContext, "[REDISPATCH] Checking if driver cancellation is eligible for redispatch");

    States source = context.getTransition().getSource().getId();

    dispatchInfo(log, requestContext, String.format("[REDISPATCH] Source state %s", source));

    if (source != States.DRIVER_ASSIGNED) {
      return false;
    }
    Long cityId = dispatchContext.getCityId();

    boolean enabled = configurationCache.getConfigAsBoolean(ClientType.CONSOLE, REDISPATCH_ON_CANCEL_CONFIG_KEY,
      "enabled", cityId);

    dispatchInfo(log, requestContext, String.format("[REDISPATCH] Configuration enabled: %s", enabled));

    return enabled;
  }
}
