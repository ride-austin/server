package com.rideaustin.dispatch.messages;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageHeaders;
import org.springframework.web.context.request.async.DeferredResult;

public class RideAcceptMessage extends UserIdAwareMessage {

  public RideAcceptMessage(DeferredResult<ResponseEntity<Object>> result, Long userId) {
    super(result, userId);
  }

  public RideAcceptMessage(MessageHeaders messageHeaders) {
    super(messageHeaders);
  }
}
