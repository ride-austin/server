package com.rideaustin.utils.dispatch;

import java.util.ArrayList;
import java.util.Optional;

import javax.annotation.Nullable;

import org.springframework.core.env.Environment;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;

import com.rideaustin.model.ride.Ride;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.service.model.context.RideFlowContext;
import com.rideaustin.service.model.context.RideRequestContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class StateMachineUtils {

  private static final String FLOW_CONTEXT_KEY = "flowContext";

  private StateMachineUtils() {}

  public static String getMachineId(Environment environment, StateMachine<States, Events> machine) {
    return getMachineId(environment, getRideId(machine.getExtendedState()));
  }

  public static String getMachineId(Environment environment, Long rideId) {
    return String.format("%s:ride:%s", environment.getProperty("cache.redis.key.prefix", String.class, ""), rideId.toString());
  }

  @Nullable
  public static Long getRideId(StateContext<States, Events> context) {
    return getRideId(context.getExtendedState());
  }

  @Nullable
  public static Long getRideId(ExtendedState extendedState) {
    return Optional.ofNullable(getRequestContext(extendedState))
      .map(RideRequestContext::getRideId)
      .orElse((Long) extendedState.getVariables().get("rideId"));
  }

  public static RideRequestContext getRequestContext(StateContext<States, Events> context) {
    return getRequestContext(context.getExtendedState());
  }

  public static RideRequestContext getRequestContext(ExtendedState extendedState) {
    return extendedState.get("requestContext", RideRequestContext.class);
  }

  public static void updateRequestContext(StateContext<States, Events> context, RideRequestContext requestContext,
    DefaultStateMachinePersister<States, Events, String> access, Environment env) {
    context.getStateMachine().getExtendedState().getVariables().put("requestContext", requestContext);
    updateMachine(context, access, env);
  }

  public static DispatchContext getDispatchContext(StateContext<States, Events> context) {
    return getDispatchContext(context.getExtendedState());
  }

  public static void updateDispatchContext(StateContext<States, Events> context, DispatchContext dispatchContext,
    DefaultStateMachinePersister<States, Events, String> access, Environment env) {
    context.getStateMachine().getExtendedState().getVariables().put("dispatchContext", dispatchContext);
    updateMachine(context, access, env);
  }

  public static DispatchContext getDispatchContext(ExtendedState extendedState) {
    return extendedState.get("dispatchContext", DispatchContext.class);
  }

  public static RideFlowContext getFlowContext(StateContext<States, Events> context) {
    return getFlowContext(context.getExtendedState());
  }

  public static void updateFlowContext(StateContext<States, Events> context, RideFlowContext flowContext,
    DefaultStateMachinePersister<States, Events, String> access, Environment env) {
    context.getStateMachine().getExtendedState().getVariables().put(FLOW_CONTEXT_KEY, flowContext);
    updateMachine(context, access, env);
  }

  public static void updateFlowContext(StateMachineContext<States, Events> context, RideFlowContext flowContext, Environment environment, StateMachinePersist<States, Events, String> contextAccess) {
    context.getExtendedState().getVariables().put(FLOW_CONTEXT_KEY, flowContext);
    try {
      contextAccess.write(context, getMachineId(environment, getRideId(context.getExtendedState())));
    } catch (Exception e) {
      log.error("Failed to update flow context", e);
    }
  }

  public static RideFlowContext getFlowContext(ExtendedState extendedState) {
    return Optional.ofNullable(extendedState.get(FLOW_CONTEXT_KEY, RideFlowContext.class)).orElse(new RideFlowContext());
  }

  public static StateMachineContext<States, Events> getPersistedContext(Environment environment, StateMachinePersist<States, Events, String> access, long rideId) {
    String machineId = getMachineId(environment, rideId);
    try {
      return access.read(machineId);
    } catch (Exception e) {
      log.error("Error reading context", e);
      return null;
    }
  }

  public static void updatePersistedContext(StateMachineContext<States, Events> context, Environment environment, StateMachinePersist<States, Events, String> access, long rideId) {
    String machineId = getMachineId(environment, rideId);
    try {
      access.write(context, machineId);
    } catch (Exception e) {
      log.error("Failed to update persisted context", e);
    }
  }

  public static RideRequestContext createRequestContext(Ride ride, int searchRadius, String directConnectId) {
    RideRequestContext requestContext = new RideRequestContext(ride.getId(), ride.getRider().getId(), ride.getStartLocationLat(), ride.getStartLocationLong(),
      ride.getCityId(), ride.getRequestedCarType().getCarCategory(), ride.getRequestedCarType().getBitmask(), new ArrayList<>(),
      searchRadius, ride.getApplePayToken());

    Optional.ofNullable(ride.getRequestedDriverTypeBitmask()).ifPresent(requestContext::setRequestedDriverTypeBitmask);
    if (directConnectId != null) {
      requestContext.setDirectConnectId(directConnectId);
    }
    return requestContext;
  }

  public static RideRequestContext createRequestContext(Ride ride, int searchRadius) {
    return createRequestContext(ride, searchRadius, null);
  }

  private static void updateMachine(StateContext<States, Events> context,
    DefaultStateMachinePersister<States, Events, String> access, Environment env) {
    try {
      access.persist(context.getStateMachine(), getMachineId(env, context.getStateMachine()));
    } catch (Exception e) {
      log.error("Error while updating machine state", e);
    }
  }

}
