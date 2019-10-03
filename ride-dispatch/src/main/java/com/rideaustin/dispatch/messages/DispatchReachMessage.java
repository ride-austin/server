package com.rideaustin.dispatch.messages;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageHeaders;
import org.springframework.web.context.request.async.DeferredResult;

public class DispatchReachMessage extends UserIdAwareMessage {
  public DispatchReachMessage(MessageHeaders messageHeaders) {
    super(messageHeaders);
  }

  public DispatchReachMessage(DeferredResult<ResponseEntity<Object>> result, Long userId) {
    super(result, userId);
  }
}
