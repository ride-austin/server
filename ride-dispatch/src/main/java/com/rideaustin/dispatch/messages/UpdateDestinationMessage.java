package com.rideaustin.dispatch.messages;

import org.springframework.messaging.MessageHeaders;
import org.springframework.web.context.request.async.DeferredResult;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.rest.model.RideEndLocation;

public class UpdateDestinationMessage extends UserIdAwareMessage {

  public UpdateDestinationMessage(MessageHeaders messageHeaders) {
    super(messageHeaders);
  }

  public UpdateDestinationMessage(DeferredResult result, Long userId, RideEndLocation endLocation) {
    super(result, userId, ImmutableMap.of("endLocation", endLocation));
  }

  public RideEndLocation getEndLocation() {
    return get("endLocation", RideEndLocation.class);
  }
}
