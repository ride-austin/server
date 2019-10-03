package com.rideaustin.dispatch.actions;

import static com.rideaustin.dispatch.LogUtil.flowInfo;

import java.util.HashMap;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;

import com.rideaustin.dispatch.messages.UpdateDestinationMessage;
import com.rideaustin.dispatch.service.RideFlowStateMachineProvider;
import com.rideaustin.model.Address;
import com.rideaustin.model.enums.EventType;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.model.MobileDriverRideDto;
import com.rideaustin.rest.model.RideEndLocation;
import com.rideaustin.service.MapService;
import com.rideaustin.service.StackedDriverRegistry;
import com.rideaustin.service.event.EventsNotificationService;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.service.model.context.RideFlowContext;
import com.rideaustin.utils.dispatch.StateMachineUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdateDestinationAction extends AbstractContextPersistingAction implements AddressAwareAction {

  @Inject
  private MapService mapService;
  @Inject
  private EventsNotificationService eventsNotificationService;
  @Inject
  private RideDslRepository rideDslRepository;
  @Inject
  private StackedDriverRegistry stackedDriverRegistry;
  @Inject
  private RideFlowStateMachineProvider machineProvider;

  @Override
  public void execute(StateContext<States, Events> context) {
    RideEndLocation endLocation = new UpdateDestinationMessage(context.getMessageHeaders()).getEndLocation();
    Address endAddress = getAddress(endLocation, mapService);
    Long rideId = StateMachineUtils.getRideId(context);
    DispatchContext dispatchContext = StateMachineUtils.getDispatchContext(context);
    Ride ride = rideDslRepository.findOne(rideId);
    ride.fillEndLocation(endLocation, endAddress);
    rideDslRepository.save(ride);

    if (dispatchContext != null && dispatchContext.getCandidate() != null) {
      eventsNotificationService.sendRideUpdateToDriver(ride, dispatchContext.getCandidate(), EventType.END_LOCATION_UPDATED);
    }

    final RideFlowContext flowContext = StateMachineUtils.getFlowContext(context);
    flowContext.increaseDestinationUpdatesCount();
    StateMachineUtils.updateFlowContext(context, flowContext, persister, environment);

    handleNextRideRedispatch(dispatchContext, ride);
  }

  private void handleNextRideRedispatch(DispatchContext dispatchContext, Ride ride) {
    if (ride.getStatus() != RideStatus.ACTIVE || dispatchContext == null || dispatchContext.getCandidate() == null) {
      return;
    }
    long activeDriverId = dispatchContext.getCandidate().getId();
    boolean stacked = stackedDriverRegistry.isStacked(activeDriverId);
    MobileDriverRideDto nextRide = rideDslRepository.findNextRide(activeDriverId);
    if (stacked && nextRide != null) {
      Long nextRideId = nextRide.getId();
      Events event = Events.FORCE_REDISPATCH;
      Optional<StateMachine<States, Events>> nextMachine = machineProvider.restoreMachine(nextRideId, event, new MessageHeaders(new HashMap<>()));
      if (nextMachine.isPresent()) {
        StateMachine<States, Events> machine = nextMachine.get();
        boolean eventSent = machine.sendEvent(new GenericMessage<>(event, new MessageHeaders(new HashMap<>())));
        if (eventSent) {
          flowInfo(log, nextRideId, String.format("Event %s successfully sent", event));
        } else {
          log.error(String.format("[Ride #%d] Failed to send event %s, current state %s. Context: %s", nextRideId, event,
            machine.getState().getId(), machine.getExtendedState()));
        }
      }
    }
  }
}
