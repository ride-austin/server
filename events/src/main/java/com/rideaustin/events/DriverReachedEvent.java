package com.rideaustin.events;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

public class DriverReachedEvent extends ApplicationEvent implements MessagingEvent {

  @Getter
  private final long rideId;

  public DriverReachedEvent(Object source, long rideId) {
    super(source);
    this.rideId = rideId;
  }
}
