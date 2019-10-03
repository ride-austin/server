package com.rideaustin;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.action.ActionListener;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.security.SecurityRule;
import org.springframework.statemachine.state.PseudoState;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.state.StateListener;
import org.springframework.statemachine.support.AbstractStateMachine;
import org.springframework.statemachine.transition.Transition;
import org.springframework.statemachine.transition.TransitionKind;
import org.springframework.statemachine.trigger.Trigger;

import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;

public class StubStateContext implements StateContext<States, Events> {

  private StubExtendedState extendedState;
  private StateMachine<States, Events> machine;
  private MessageHeaders messageHeaders;

  private State<States, Events> sourceState;
  private State<States, Events> targetState;

  public StubStateContext() {
    this(null);
  }

  public StubStateContext(States initialState) {
    this.extendedState = new StubExtendedState();
    this.machine = new StubStateMachine(extendedState, initialState);
    this.messageHeaders = new MessageHeaders(new HashMap<>());
  }

  @Override
  public Stage getStage() {
    return null;
  }

  @Override
  public Message<Events> getMessage() {
    return null;
  }

  @Override
  public Events getEvent() {
    return null;
  }

  @Override
  public MessageHeaders getMessageHeaders() {
    return messageHeaders;
  }

  public void addMessageHeader(String name, Object value) {
    Map<String, Object> headers = new HashMap<>(messageHeaders);
    headers.put(name, value);
    messageHeaders = new MessageHeaders(headers);
  }

  @Override
  public Object getMessageHeader(Object header) {
    return messageHeaders.get(header);
  }

  @Override
  public ExtendedState getExtendedState() {
    return extendedState;
  }

  @Override
  public Transition<States, Events> getTransition() {
    return new Transition<States, Events>() {
      @Override
      public boolean transit(StateContext<States, Events> context) {
        return false;
      }

      @Override
      public void executeTransitionActions(StateContext<States, Events> context) {}

      @Override
      public State<States, Events> getSource() {
        return sourceState;
      }

      @Override
      public State<States, Events> getTarget() {
        return targetState;
      }

      @Override
      public Guard<States, Events> getGuard() {
        return null;
      }

      @Override
      public Collection<Action<States, Events>> getActions() {
        return null;
      }

      @Override
      public Trigger<States, Events> getTrigger() {
        return null;
      }

      @Override
      public TransitionKind getKind() {
        return null;
      }

      @Override
      public SecurityRule getSecurityRule() {
        return null;
      }

      @Override
      public void addActionListener(ActionListener<States, Events> listener) {}

      @Override
      public void removeActionListener(ActionListener<States, Events> listener) {}
    };
  }

  @Override
  public StateMachine<States, Events> getStateMachine() {
    return machine;
  }

  @Override
  public State<States, Events> getSource() {
    return sourceState;
  }

  @Override
  public Collection<State<States, Events>> getSources() {
    return null;
  }

  @Override
  public State<States, Events> getTarget() {
    return targetState;
  }

  @Override
  public Collection<State<States, Events>> getTargets() {
    return null;
  }

  @Override
  public Exception getException() {
    return null;
  }

  public void setSource(States states) {
    this.sourceState = new StateAdapter(states);
  }

  public void setTarget(States states) {
    this.targetState = new StateAdapter(states);
  }

  public static class StubExtendedState implements ExtendedState {

    private Map<Object, Object> variables = new HashMap<>();

    public StubExtendedState() {
    }

    public StubExtendedState(Map<Object, Object> variables) {
      this.variables = variables;
    }

    @Override
    public Map<Object, Object> getVariables() {
      return variables;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
      Object value = this.variables.get(key);
      if (value == null) {
        return null;
      }
      if (!type.isAssignableFrom(value.getClass())) {
        throw new IllegalArgumentException("Incorrect type specified for variable '" +
          key + "'. Expected [" + type + "] but actual type is [" + value.getClass() + "]");
      }
      return (T) value;
    }

    @Override
    public void setExtendedStateChangeListener(ExtendedStateChangeListener listener) {

    }
  }

  public static class StubStateMachine extends AbstractStateMachine<States, Events> {

    private ExtendedState extendedState;
    private boolean stopped = false;
    private Events lastSentEvent;
    private boolean shouldSendEvent = true;

    public StubStateMachine(ExtendedState extendedState, States initialState) {
      super(Collections.emptyList(), Collections.emptyList(), initialState == null ? null : new StateAdapter(initialState));
      this.extendedState = extendedState;
    }

    @Override
    public ExtendedState getExtendedState() {
      return extendedState;
    }

    @Override
    protected void doStart() {

    }

    @Override
    public boolean sendEvent(Events event) {
      lastSentEvent = event;
      super.sendEvent(event);
      return shouldSendEvent;
    }

    @Override
    protected void doStop() {
      this.stopped = true;
    }

    public boolean isStopped() {
      return stopped;
    }

    public Events getLastSentEvent() {
      return lastSentEvent;
    }

    public void setShouldSendEvent(boolean shouldSendEvent) {
      this.shouldSendEvent = shouldSendEvent;
    }
  }

  private static class StateAdapter implements State<States, Events> {

    private final States id;

    private StateAdapter(States id) {
      this.id = id;
    }

    @Override
    public boolean sendEvent(Message<Events> event) {
      return false;
    }

    @Override
    public boolean shouldDefer(Message<Events> event) {
      return false;
    }

    @Override
    public void exit(StateContext<States, Events> context) {}

    @Override
    public void entry(StateContext<States, Events> context) {}

    @Override
    public States getId() {
      return id;
    }

    @Override
    public Collection<States> getIds() {
      return null;
    }

    @Override
    public Collection<State<States, Events>> getStates() {
      return null;
    }

    @Override
    public PseudoState<States, Events> getPseudoState() {
      return null;
    }

    @Override
    public Collection<Events> getDeferredEvents() {
      return null;
    }

    @Override
    public Collection<? extends Action<States, Events>> getEntryActions() {
      return null;
    }

    @Override
    public Collection<? extends Action<States, Events>> getStateActions() {
      return null;
    }

    @Override
    public Collection<? extends Action<States, Events>> getExitActions() {
      return null;
    }

    @Override
    public boolean isSimple() {
      return false;
    }

    @Override
    public boolean isComposite() {
      return false;
    }

    @Override
    public boolean isOrthogonal() {
      return false;
    }

    @Override
    public boolean isSubmachineState() {
      return false;
    }

    @Override
    public void addStateListener(StateListener<States, Events> listener) {}

    @Override
    public void removeStateListener(StateListener<States, Events> listener) {}

    @Override
    public void addActionListener(ActionListener<States, Events> listener) {}

    @Override
    public void removeActionListener(ActionListener<States, Events> listener) {}
  }
}
