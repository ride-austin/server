package com.rideaustin.dispatch.actions;

import javax.inject.Inject;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;

import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.dispatch.messages.UpdateCommentMessage;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.utils.dispatch.StateMachineUtils;
import com.rideaustin.model.enums.EventType;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.service.event.EventsNotificationService;

public class UpdateCommentAction implements Action<States, Events> {

  @Inject
  private RideDslRepository rideDslRepository;
  @Inject
  private EventsNotificationService eventsNotificationService;

  @Override
  public void execute(StateContext<States, Events> context) {
    Long rideId = StateMachineUtils.getRideId(context);
    DispatchContext dispatchContext = StateMachineUtils.getDispatchContext(context);
    Ride ride = rideDslRepository.findOne(rideId);
    ride.setComment(new UpdateCommentMessage(context.getMessageHeaders()).getComment());
    rideDslRepository.save(ride);

    if (dispatchContext != null && dispatchContext.getCandidate() != null) {
      eventsNotificationService.sendRideUpdateToDriver(ride, dispatchContext.getCandidate(), EventType.RIDER_COMMENT_UPDATED);
    }
  }
}
