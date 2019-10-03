package com.rideaustin.dispatch.guards;

import java.util.Objects;

import javax.inject.Inject;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;

import com.rideaustin.dispatch.messages.UpdateCommentMessage;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.ride.RideOwnerService;
import com.rideaustin.utils.dispatch.StateMachineUtils;

public class UpdateCommentGuard implements Guard<States, Events> {

  @Inject
  private RideOwnerService rideOwnerService;
  @Inject
  private RideDslRepository rideDslRepository;

  @Override
  public boolean evaluate(StateContext<States, Events> context) {
    Long rideId = StateMachineUtils.getRideId(context);
    Ride ride = rideDslRepository.findOne(rideId);

    UpdateCommentMessage message = new UpdateCommentMessage(context.getMessageHeaders());

    String comment = message.getComment();
    Long userId = message.getUserId();
    return !Objects.equals(ride.getComment(), comment) && rideOwnerService.isRideRider(userId, rideId);
  }
}
