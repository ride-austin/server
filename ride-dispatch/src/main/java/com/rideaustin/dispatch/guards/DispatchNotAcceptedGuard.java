package com.rideaustin.dispatch.guards;

import static com.rideaustin.dispatch.LogUtil.dispatchInfo;

import java.util.Date;
import java.util.EnumSet;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.core.env.Environment;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.guard.Guard;

import com.rideaustin.application.cache.impl.JedisClient;
import com.rideaustin.service.config.RideAcceptanceConfig;
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.service.model.context.RideRequestContext;
import com.rideaustin.utils.dispatch.StateMachineUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DispatchNotAcceptedGuard implements Guard<States, Events> {

  @Inject
  private JedisClient jedisClient;
  @Inject
  private Environment environment;
  @Inject
  private StateMachinePersist<States, Events, String> persist;
  @Inject
  private RideAcceptanceConfig acceptanceConfig;

  @Override
  public boolean evaluate(StateContext<States, Events> context) {
    RideRequestContext requestContext = StateMachineUtils.getRequestContext(context);
    dispatchInfo(log, requestContext, String.format("Evaluating transition from %s to %s", context.getSource().getId(), context.getTarget().getId()));
    Long rideId = StateMachineUtils.getRideId(context);
    String machineId = StateMachineUtils.getMachineId(environment, rideId);
    if (!jedisClient.exists(machineId)) {
      dispatchInfo(log, requestContext,"Ride state is not found, aborting machine");
      context.getStateMachine().stop();
      return false;
    }
    StateMachineContext<States, Events> persistedContext = StateMachineUtils.getPersistedContext(environment, persist, rideId);
    if (persistedContext != null) {
      DispatchContext dispatchContext = StateMachineUtils.getDispatchContext(persistedContext.getExtendedState());
      boolean notAccepted = !Optional.ofNullable(dispatchContext).map(DispatchContext::isAccepted).orElse(true);
      dispatchInfo(log, requestContext, String.format("Dispatch context is present but not accepted by driver: %s", notAccepted));
      Optional<Date> requestedAt = Optional.ofNullable(dispatchContext)
        .map(DispatchContext::getCandidate)
        .map(DispatchCandidate::getRequestedAt);
      dispatchInfo(log, requestContext, String.format("Current candidate is requested at: %s", requestedAt.isPresent() ? requestedAt.get() : "missing"));
      boolean expired = requestedAt
        .map(d -> {
          long delta = System.currentTimeMillis() - d.getTime();
          long timeout = acceptanceConfig.getDriverAcceptancePeriod(requestContext.getCityId()) * 1000L;
          dispatchInfo(log, requestContext, String.format("Time passed from requesting candidate: %d", delta));
          dispatchInfo(log, requestContext, String.format("Total timeout: %d", timeout));
          return delta >= timeout;
        })
        .orElse(true);
      if (dispatchContext != null && dispatchContext.getCandidate() != null) {
        dispatchInfo(log, requestContext, "Current candidate id:" + dispatchContext.getCandidate().getId());
      }
      dispatchInfo(log, requestContext, String.format("Current candidate request is expired: %s", expired));
      dispatchInfo(log, requestContext, String.format("Current ride state is: %s", persistedContext.getState()));
      return expired && notAccepted && EnumSet.of(States.REQUESTED, States.DISPATCH_PENDING).contains(persistedContext.getState());
    }
    dispatchInfo(log, requestContext, "Persisted context is not found, aborting transition");
    return false;
  }
}
