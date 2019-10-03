package com.rideaustin.events;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

public class RideAcceptedEvent extends ApplicationEvent implements MessagingEvent {

  @Getter
  private final long rideId;

  public RideAcceptedEvent(long rideId) {
    super(new Object());
    this.rideId = rideId;
  }

}
