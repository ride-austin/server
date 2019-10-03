package com.rideaustin.dispatch.service;

import java.util.Optional;

import javax.inject.Inject;

import org.springframework.core.env.Environment;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.statemachine.state.State;

import com.rideaustin.dispatch.InceptionMachinesTracker;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.utils.dispatch.StateMachineUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RideFlowStateMachineListener extends StateMachineListenerAdapter<States, Events> {

  @Inject
  private StateMachinePersister<States, Events, String> persister;
  @Inject
  private StateMachinePersist<States, Events, String> contextAccess;
  @Inject
  private Environment environment;
  @Inject
  private InceptionMachinesTracker machineTracker;

  @Override
  public void stateContext(StateContext<States, Events> stateContext) {
    super.stateContext(stateContext);
    States source = safeState(stateContext.getSource());
    States target = safeState(stateContext.getTarget());
    StateMachine<States, Events> machine = stateContext.getStateMachine();
    Long rideId = StateMachineUtils.getRideId(stateContext);
    if (rideId == null || machine.getState() == null) {
      return;
    }
    if (source != target && stateContext.getStage() == StateContext.Stage.TRANSITION_END) {
      log.info(String.format("[Machine %d] State changed from %s to %s", rideId, source, target));
    }
    String machineId = StateMachineUtils.getMachineId(environment, machine);
    try {
      StateMachineContext<States, Events> persistedContext = contextAccess.read(machineId);
      if (persistedContext == null || persistedContext.getState().isBefore(machine.getState().getId()) || persistedContext.getState().equals(machine.getState().getId())) {
        persister.persist(machine, machineId);
      } else {
        log.error(String.format("[Machine %d] Ride is persisted as %s, abort persisting it as %s", rideId, persistedContext.getState(), machine.getState().getId()));
      }
    } catch (Exception e) {
      log.error(String.format("[Machine %s] Ride %s failed to persist", stateContext.getStateMachine().getUuid(), rideId), e);
    }
  }

  @Override
  public void stateMachineStarted(StateMachine<States, Events> stateMachine) {
    super.stateMachineStarted(stateMachine);
    Long rideId = StateMachineUtils.getRideId(stateMachine.getExtendedState());
    String machineId = StateMachineUtils.getMachineId(environment, rideId);
    try {
      persister.persist(stateMachine, machineId);
    } catch (Exception e) {
      log.error(String.format("[Machine %s] Failed to persist state machine on start", machineId), e);
    }
    if (Boolean.TRUE.equals(stateMachine.getExtendedState().get("_inception", Boolean.class))) {
      machineTracker.startTracking(rideId, stateMachine);
    }
  }

  private static States safeState(State<States, Events> state) {
    return Optional.ofNullable(state).map(State::getId).orElse(null);
  }
}