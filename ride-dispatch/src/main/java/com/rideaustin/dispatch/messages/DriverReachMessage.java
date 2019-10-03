package com.rideaustin.dispatch.messages;

import java.util.Date;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageHeaders;
import org.springframework.web.context.request.async.DeferredResult;

import com.google.common.collect.ImmutableMap;

public class DriverReachMessage extends DeferredResultMessage<ResponseEntity<Object>> {

  private static final String REACHED_DATE = "reachedDate";

  public DriverReachMessage(MessageHeaders messageHeaders) {
    super(messageHeaders);
  }

  public DriverReachMessage(Date reachedDate) {
    super(ImmutableMap.of(REACHED_DATE, reachedDate));
  }

  public DriverReachMessage(DeferredResult<ResponseEntity<Object>> result) {
    super(result, ImmutableMap.of(REACHED_DATE, new Date()));
  }

  public Date getReachedDate() {
    return get(REACHED_DATE, Date.class);
  }
}
