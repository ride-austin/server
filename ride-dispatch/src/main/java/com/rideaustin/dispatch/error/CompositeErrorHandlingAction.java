package com.rideaustin.dispatch.error;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;

import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;

public class CompositeErrorHandlingAction implements Action<States, Events> {

  private final List<Action<States, Events>> actions;

  public CompositeErrorHandlingAction(ErrorHandlingAction errorHandlingAction, Action<States, Events>... actions) {
    this.actions = new ArrayList<>();
    this.actions.addAll(Arrays.asList(actions));
    this.actions.add(errorHandlingAction);
  }

  @Override
  public void execute(StateContext<States, Events> context) {
    for (Action<States, Events> action : actions) {
      action.execute(context);
    }
  }
}
