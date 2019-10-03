package com.rideaustin.dispatch.error;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;

import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;

public class StopMachineAction implements Action<States, Events> {
  @Override
  public void execute(StateContext<States, Events> context) {
    context.getStateMachine().stop();
  }
}
