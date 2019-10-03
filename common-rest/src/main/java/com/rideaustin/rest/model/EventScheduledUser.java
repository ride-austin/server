package com.rideaustin.rest.model;

import java.util.List;

import org.springframework.web.context.request.async.DeferredResult;

import com.rideaustin.model.user.User;

public class EventScheduledUser {

  private User user;
  private Long lastReceivedEvent;
  private DeferredResult<List<EventDto>> deferredResult;

  public EventScheduledUser() {
  }

  public EventScheduledUser(User user, Long lastReceivedEvent, DeferredResult<List<EventDto>> deferredResult) {
    this.user = user;
    this.lastReceivedEvent = lastReceivedEvent;
    this.deferredResult = deferredResult;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public DeferredResult<List<EventDto>> getDeferredResult() {
    return deferredResult;
  }

  public void setDeferredResult(DeferredResult<List<EventDto>> deferredResult) {
    this.deferredResult = deferredResult;
  }

  public Long getLastReceivedEvent() {
    return lastReceivedEvent;
  }

  public void setLastReceivedEvent(Long lastReceivedEvent) {
    this.lastReceivedEvent = lastReceivedEvent;
  }
}
