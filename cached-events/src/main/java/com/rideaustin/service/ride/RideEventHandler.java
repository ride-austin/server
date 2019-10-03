package com.rideaustin.service.ride;

@FunctionalInterface
public interface RideEventHandler<E extends RideEvent> {
  void handle(E event);
}
