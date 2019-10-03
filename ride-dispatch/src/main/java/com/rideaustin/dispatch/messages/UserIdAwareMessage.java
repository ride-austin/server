package com.rideaustin.dispatch.messages;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageHeaders;
import org.springframework.web.context.request.async.DeferredResult;

import com.google.common.collect.ImmutableMap;

public abstract class UserIdAwareMessage extends DeferredResultMessage<ResponseEntity<Object>> {

  private static final String USER_ID_KEY = "userId";

  protected UserIdAwareMessage(MessageHeaders messageHeaders) {
    super(messageHeaders);
  }

  public UserIdAwareMessage(DeferredResult<ResponseEntity<Object>> result, Long userId) {
    super(result, ImmutableMap.of(USER_ID_KEY, userId));
  }

  public UserIdAwareMessage(DeferredResult<ResponseEntity<Object>> result, Long userId, Map<String, Object> params) {
    super(result, ImmutableMap.<String, Object>builder()
      .put(USER_ID_KEY, userId)
      .putAll(params)
      .build()
    );
  }

  public Long getUserId() {
    return get(USER_ID_KEY, Long.class);
  }
}
