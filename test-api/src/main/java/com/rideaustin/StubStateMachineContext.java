package com.rideaustin;

import java.util.List;
import java.util.Map;

import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateMachineContext;

import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;

public class StubStateMachineContext implements StateMachineContext<States, Events> {

  private final Map<Object, Object> state;
  private final States currentState;

  public StubStateMachineContext(Map<Object, Object> state) {
    this(state, null);
  }

  public StubStateMachineContext(Map<Object, Object> state, States currentState) {
    this.state = state;
    this.currentState = currentState;
  }

  @Override
  public String getId() {
    return null;
  }

  @Override
  public List<StateMachineContext<States, Events>> getChilds() {
    return null;
  }

  @Override
  public States getState() {
    return currentState;
  }

  @Override
  public Events getEvent() {
    return null;
  }

  @Override
  public Map<States, States> getHistoryStates() {
    return null;
  }

  @Override
  public Map<String, Object> getEventHeaders() {
    return null;
  }

  @Override
  public ExtendedState getExtendedState() {
    return new StubStateContext.StubExtendedState(state);
  }
}
