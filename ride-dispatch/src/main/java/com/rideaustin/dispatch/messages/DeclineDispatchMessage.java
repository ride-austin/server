package com.rideaustin.dispatch.messages;

import org.springframework.messaging.MessageHeaders;
import org.springframework.web.context.request.async.DeferredResult;

public class DeclineDispatchMessage extends UserIdAwareMessage {

  public DeclineDispatchMessage(MessageHeaders messageHeaders) {
    super(messageHeaders);
  }

  public DeclineDispatchMessage(Long userId) {
    super(new DeferredResult<>(), userId);
  }

}
