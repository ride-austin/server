package com.rideaustin.service.ride;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import com.rideaustin.dispatch.LogUtil;
import com.rideaustin.dispatch.messages.DriverReachMessage;
import com.rideaustin.dispatch.messages.EndRideMessage;
import com.rideaustin.dispatch.messages.RideStartMessage;
import com.rideaustin.dispatch.service.RideFlowStateMachineProvider;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.ride.events.CachedEventType;
import com.rideaustin.service.ride.events.EndRideEvent;
import com.rideaustin.service.ride.events.UpdateRideLocationEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SequentialRideEventsDispatcher {

  private final RideEventHandler<UpdateRideLocationEvent> locationUpdateEventHandler;
  private final RideFlowStateMachineProvider machineProvider;

  public void dispatchEvents(List<RideEvent> rideEvents, DeferredResult<ResponseEntity> result) {
    TreeMap<Long, List<RideEvent>> ridesMap = rideEvents
      .stream()
      .collect(Collectors.groupingBy(RideEvent::getRideId, TreeMap::new, Collectors.toList()));
    int ridesCount = 0;
    for (Map.Entry<Long, List<RideEvent>> entry : ridesMap.entrySet()) {
      Long rideId = entry.getKey();
      ridesCount++;
      log.info(String.format("[EVENTS %d] Received %d cached events", rideId, entry.getValue().size()));
      for (RideEvent event : entry.getValue()) {
        log.info(String.format("[EVENTS %d] %s", rideId, event.toString()));
      }
      Map<Boolean, List<RideEvent>> partitionedEvents = entry.getValue()
        .stream()
        .collect(Collectors.partitioningBy(e -> CachedEventType.UPDATE_LOCATION.equals(e.getEventType())));
      List<RideEvent> locationUpdateEvents = partitionedEvents.getOrDefault(true, new ArrayList<>());
      log.info(String.format("[EVENTS %d] Received %d LU events", rideId, locationUpdateEvents.size()));
      List<RideEvent> rideFlowEvents = partitionedEvents.getOrDefault(false, new ArrayList<>());
      log.info(String.format("[EVENTS %d] Received %d RF events", rideId, rideFlowEvents.size()));

      if (!locationUpdateEvents.isEmpty()) {
        for (RideEvent event : locationUpdateEvents) {
          locationUpdateEventHandler.handle((UpdateRideLocationEvent) event);
        }
      }

      if (!rideFlowEvents.isEmpty()) {
        processRideFlowEvents(ridesMap, ridesCount, rideId, rideFlowEvents, result);
      }
    }
  }

  private void processRideFlowEvents(Map<Long, List<RideEvent>> ridesMap, int ridesCount, Long rideId,
    List<RideEvent> rideFlowEvents, DeferredResult<ResponseEntity> result) {
    CountingStateListener listener = new CountingStateListener(result);
    RideEvent firstEvent = rideFlowEvents.get(0);
    log.info(String.format("[EVENTS %d] First event is %s", rideId, firstEvent.toString()));
    MessageHeaders headers = createHeaders(firstEvent);
    Optional<StateMachine<States, Events>> restored = machineProvider.restoreMachine(rideId, firstEvent.getEventType().getRideFlowEvent(), headers);
    if (restored.isPresent()) {
      log.info(String.format("[EVENTS %d] Flow machine restored", rideId));
      StateMachine<States, Events> machine = restored.get();
      listener.addExpectedTransitions(rideFlowEvents.size());
      listener.shouldSetResult(ridesCount == ridesMap.keySet().size());
      machine.addStateListener(listener);
      for (RideEvent nextEvent : rideFlowEvents) {
        boolean sent = machine.sendEvent(new GenericMessage<>(nextEvent.getEventType().getRideFlowEvent(), createHeaders(nextEvent)));
        if (sent) {
          log.info(String.format("[EVENTS %d] Event %s sent", rideId, nextEvent.getEventType().getRideFlowEvent()));
        } else {
          log.info(String.format("[EVENTS %d] Event %s failed to send", rideId, nextEvent.getEventType().getRideFlowEvent()));
        }
      }
      machine.removeStateListener(listener);
    } else {
      LogUtil.flowInfo(log, rideId, "Failed to restore machine for ride for cached events processing");
    }
  }

  private MessageHeaders createHeaders(RideEvent event) {
    switch (event.getEventType()) {
      case DRIVER_REACHED:
        return new DriverReachMessage(new Date(event.getTimestamp()));
      case END_RIDE:
        return new EndRideMessage(new Date(event.getTimestamp()), ((EndRideEvent) event).getEndLocation());
      case START_RIDE:
        return new RideStartMessage(new Date(event.getTimestamp()));
      default:
        return new MessageHeaders(Collections.emptyMap());
    }
  }

  static class CountingStateListener extends StateMachineListenerAdapter<States, Events> {

    private final DeferredResult<ResponseEntity> result;

    private int expectedTransitions = 0;
    private int occurredTransitions = 0;
    private boolean shouldSetResult;

    CountingStateListener(DeferredResult<ResponseEntity> result) {
      this.result = result;
    }

    @Override
    public void transitionEnded(Transition<States, Events> transition) {
      super.transitionEnded(transition);
      occurredTransitions++;
      if (shouldSetResult && occurredTransitions >= expectedTransitions) {
        result.setResult(ResponseEntity.ok().build());
      }
    }

    void addExpectedTransitions(int extra) {
      this.expectedTransitions += extra;
    }

    void shouldSetResult(boolean shouldSetResult) {
      this.shouldSetResult = shouldSetResult;
    }
  }
}
