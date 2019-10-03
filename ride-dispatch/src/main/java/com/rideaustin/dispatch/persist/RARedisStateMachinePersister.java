package com.rideaustin.dispatch.persist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.region.Region;
import org.springframework.statemachine.state.AbstractState;
import org.springframework.statemachine.state.HistoryPseudoState;
import org.springframework.statemachine.state.PseudoState;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.AbstractStateMachine;
import org.springframework.statemachine.support.DefaultExtendedState;
import org.springframework.statemachine.support.DefaultStateMachineContext;

import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;

public class RARedisStateMachinePersister extends DefaultStateMachinePersister<States, Events, String> {
  /**
   * Instantiates a new redis state machine persister.
   *
   * @param stateMachinePersist the state machine persist
   */
  public RARedisStateMachinePersister(StateMachinePersist<States, Events, String> stateMachinePersist) {
    super(stateMachinePersist);
  }

  @Override
  protected StateMachineContext<States, Events> buildStateMachineContext(StateMachine<States, Events> stateMachine) {
    ExtendedState extendedState = new DefaultExtendedState();
    extendedState.getVariables().putAll(
      stateMachine.getExtendedState()
        .getVariables().entrySet()
        .stream().filter(e -> !((String)e.getKey()).startsWith("_"))
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue))
    );

    ArrayList<StateMachineContext<States, Events>> childs = new ArrayList<>();
    States id;
    State<States, Events> state = stateMachine.getState();
    if (state.isSubmachineState()) {
      id = getDeepState(state);
    } else if (state.isOrthogonal()) {
      Collection<Region<States, Events>> regions = ((AbstractState<States, Events>)state).getRegions();
      for (Region<States, Events> r : regions) {
        StateMachine<States, Events> rsm = (StateMachine<States, Events>) r;
        childs.add(buildStateMachineContext(rsm));
      }
      id = state.getId();
    } else {
      id = state.getId();
    }

    // building history state mappings
    Map<States, States> historyStates = new EnumMap<>(States.class);
    PseudoState<States, Events> historyState = ((AbstractStateMachine<States, Events>) stateMachine).getHistoryState();
    if (historyState != null) {
      historyStates.put(null, ((HistoryPseudoState<States, Events>)historyState).getState().getId());
    }
    Collection<State<States, Events>> states = stateMachine.getStates();
    for (State<States, Events> ss : states) {
      if (ss.isSubmachineState()) {
        StateMachine<States, Events> submachine = ((AbstractState<States, Events>) ss).getSubmachine();
        PseudoState<States, Events> ps = ((AbstractStateMachine<States, Events>) submachine).getHistoryState();
        if (ps != null) {
          State<States, Events> pss = ((HistoryPseudoState<States, Events>)ps).getState();
          if (pss != null) {
            historyStates.put(ss.getId(), pss.getId());
          }
        }
      }
    }
    return new DefaultStateMachineContext<>(childs, id, null, null, extendedState, historyStates, stateMachine.getId());
  }

  private States getDeepState(State<States, Events> state) {
    Collection<States> ids1 = state.getIds();
    States[] ids2 = (States[]) ids1.toArray();
    return ids2[ids2.length-1];
  }
}
