package com.rideaustin.dispatch.guards;

import javax.inject.Inject;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;

import com.rideaustin.dispatch.messages.UpdateDestinationMessage;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.utils.dispatch.StateMachineUtils;
import com.rideaustin.rest.model.RideEndLocation;
import com.rideaustin.service.ride.RideOwnerService;

public class UpdateDestinationGuard implements Guard<States, Events> {

  @Inject
  private RideOwnerService rideOwnerService;

  @Override
  public boolean evaluate(StateContext<States, Events> context) {
    UpdateDestinationMessage message = new UpdateDestinationMessage(context.getMessageHeaders());

    RideEndLocation endLocation = message.getEndLocation();
    Long rideId = StateMachineUtils.getRideId(context);
    Long userId = message.getUserId();

    boolean locationValid = endLocation.getLat() != null && endLocation.getLng() != null;
    boolean riderRide = rideOwnerService.isRideRider(userId, rideId);
    return riderRide && locationValid;
  }
}
