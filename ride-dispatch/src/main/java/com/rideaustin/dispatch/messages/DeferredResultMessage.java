package com.rideaustin.dispatch.messages;

import java.util.HashMap;
import java.util.Map;

import org.springframework.messaging.MessageHeaders;
import org.springframework.web.context.request.async.DeferredResult;

import com.google.common.collect.ImmutableMap;

public class DeferredResultMessage<T> extends MessageHeaders {

  private static final String RESULT_KEY = "result";

  public DeferredResultMessage(MessageHeaders messageHeaders) {
    super(new HashMap<>(messageHeaders));
  }

  public DeferredResultMessage(DeferredResult<T> result) {
    super(ImmutableMap.of(RESULT_KEY, result));
  }

  protected DeferredResultMessage(DeferredResult<T> result, Map<String, Object> params) {
    super(
      ImmutableMap.<String, Object>builder()
        .putAll(params)
        .put(RESULT_KEY, result)
        .build()
    );
  }

  protected DeferredResultMessage(Map<String, Object> params) {
    super(ImmutableMap.copyOf(params));
  }

  public DeferredResult<T> getDeferredResult() {
    return (DeferredResult<T>) get(RESULT_KEY, DeferredResult.class);
  }
}
