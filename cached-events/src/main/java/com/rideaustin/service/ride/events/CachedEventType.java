package com.rideaustin.service.ride.events;

import com.rideaustin.service.model.Events;
import com.rideaustin.service.ride.RideEvent;

import lombok.Getter;

@Getter
public enum CachedEventType {
  DRIVER_REACHED(DriverReachedEvent.class, Events.DRIVER_REACH),
  START_RIDE(StartRideEvent.class, Events.START_RIDE),
  UPDATE_LOCATION(UpdateRideLocationEvent.class, null),
  END_RIDE(EndRideEvent.class, Events.END_RIDE),
  ;

  private final Class<? extends RideEvent> eventClass;
  private final Events rideFlowEvent;

  CachedEventType(Class<? extends RideEvent> eventClass, Events rideFlowEvent) {
    this.eventClass = eventClass;
    this.rideFlowEvent = rideFlowEvent;
  }
}
