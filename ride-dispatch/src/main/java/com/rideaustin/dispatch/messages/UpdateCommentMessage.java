package com.rideaustin.dispatch.messages;

import org.springframework.messaging.MessageHeaders;
import org.springframework.web.context.request.async.DeferredResult;

import com.google.common.collect.ImmutableMap;

public class UpdateCommentMessage extends UserIdAwareMessage {

  public UpdateCommentMessage(MessageHeaders messageHeaders) {
    super(messageHeaders);
  }

  public UpdateCommentMessage(Long userId, String comment) {
    super(new DeferredResult<>(), userId, ImmutableMap.of("comment", comment));
  }

  public String getComment() {
    return get("comment", String.class);
  }
}
