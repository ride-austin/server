package com.rideaustin.dispatch;

import static com.rideaustin.dispatch.LogUtil.dispatchInfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.messaging.support.GenericMessage;
import org.springframework.statemachine.StateMachine;

import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InceptionMachinesTracker {

  @Getter(AccessLevel.PACKAGE)
  private Map<Long, Set<StateMachine<States, Events>>> machines = new HashMap<>();

  public void startTracking(Long rideId, StateMachine<States, Events> machine) {
    if (!machines.containsKey(rideId)) {
      machines.put(rideId, new HashSet<>());
    }
    machines.get(rideId).add(machine);
  }

  public void stopMachines(Long rideId) {
    if (machines.containsKey(rideId)) {
      Set<StateMachine<States, Events>> trackedMachines = this.machines.get(rideId);
      trackedMachines.forEach(StateMachine::stop);
      trackedMachines.clear();
      this.machines.remove(rideId);
    }
  }

  public void proxyEvent(Long rideId, GenericMessage<Events> message) {
    if (machines.containsKey(rideId)) {
      Set<StateMachine<States, Events>> trackedMachines = this.machines.get(rideId);
      dispatchInfo(log, rideId, String.format("Proxying event %s to %d machines", message.getPayload(), trackedMachines.size()));
      trackedMachines.forEach(m -> m.sendEvent(message));
    }
  }
}
