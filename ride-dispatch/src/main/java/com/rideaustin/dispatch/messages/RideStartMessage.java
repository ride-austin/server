package com.rideaustin.dispatch.messages;

import java.util.Date;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageHeaders;
import org.springframework.web.context.request.async.DeferredResult;

import com.google.common.collect.ImmutableMap;

public class RideStartMessage extends DeferredResultMessage<ResponseEntity<Object>> {

  private static final String START_DATE_KEY = "startDate";

  public RideStartMessage(MessageHeaders messageHeaders) {
    super(messageHeaders);
  }

  public RideStartMessage(Date reachedDate) {
    super(ImmutableMap.of(START_DATE_KEY, reachedDate));
  }

  public RideStartMessage(DeferredResult<ResponseEntity<Object>> result) {
    super(result, ImmutableMap.of(START_DATE_KEY, new Date()));
  }

  public Date getStartDate() {
    return get(START_DATE_KEY, Date.class);
  }
}
