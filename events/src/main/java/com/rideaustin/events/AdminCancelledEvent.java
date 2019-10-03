package com.rideaustin.events;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class AdminCancelledEvent extends ApplicationEvent {

  private final long rideId;

  public AdminCancelledEvent(Object source, long rideId) {
    super(source);
    this.rideId = rideId;
  }
}
