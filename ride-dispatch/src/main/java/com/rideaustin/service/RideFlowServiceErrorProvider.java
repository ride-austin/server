package com.rideaustin.service;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;

@Component
public class RideFlowServiceErrorProvider {

  private static final Map<States, Map<Events, Consumer<DeferredResult>>> MAPPING = new EnumMap<>(States.class);

  static {
    MAPPING.put(
      States.REQUESTED, ImmutableMap.<Events, Consumer<DeferredResult>>builder()
        .put(Events.DISPATCH_REQUEST_ACCEPT, errorMessage("Ride request is already redispatched, you can't accept it"))
        .put(Events.DISPATCH_REQUEST_DECLINE, errorMessage("Ride request is already redispatched, you can't decline it"))
        .build()
    );
    MAPPING.put(
      States.DRIVER_ASSIGNED, ImmutableMap.<Events, Consumer<DeferredResult>>builder()
        .put(Events.DISPATCH_REQUEST_ACCEPT, rideAlreadyAccepted())
        .put(Events.DISPATCH_REQUEST_DECLINE, rideAlreadyDeclined())
        .put(Events.START_RIDE, completeWithoutError())
        .put(Events.END_RIDE, completeWithoutError())
        .build()
    );
    MAPPING.put(
      States.DRIVER_REACHED, ImmutableMap.<Events, Consumer<DeferredResult>>builder()
        .put(Events.DISPATCH_REQUEST_ACCEPT, rideAlreadyAccepted())
        .put(Events.DISPATCH_REQUEST_DECLINE, rideAlreadyDeclined())
        .put(Events.DRIVER_REACH, completeWithoutError())
        .put(Events.END_RIDE, completeWithoutError())
        .build()
    );
    MAPPING.put(
      States.ACTIVE, ImmutableMap.<Events, Consumer<DeferredResult>>builder()
        .put(Events.DISPATCH_REQUEST_ACCEPT, rideAlreadyAccepted())
        .put(Events.DISPATCH_REQUEST_DECLINE, rideAlreadyDeclined())
        .put(Events.DRIVER_REACH, completeWithoutError())
        .put(Events.DRIVER_CANCEL, rideIsInProgress())
        .put(Events.RIDER_CANCEL, rideIsInProgress())
        .build()
    );
    MAPPING.put(
      States.COMPLETED, ImmutableMap.<Events, Consumer<DeferredResult>>builder()
        .put(Events.DISPATCH_REQUEST_ACCEPT, rideCompleted())
        .put(Events.DISPATCH_REQUEST_DECLINE, rideCompleted())
        .put(Events.DRIVER_REACH, rideCompleted())
        .put(Events.DRIVER_CANCEL, rideCompleted())
        .put(Events.RIDER_CANCEL, rideCompleted())
        .put(Events.START_RIDE, rideCompleted())
        .put(Events.END_RIDE, rideCompleted())
        .build());
    MAPPING.put(
      States.RIDER_CANCELLED, ImmutableMap.<Events, Consumer<DeferredResult>>builder()
        .put(Events.DISPATCH_REQUEST_ACCEPT, rideCancelled())
        .put(Events.DISPATCH_REQUEST_DECLINE, rideCancelled())
        .put(Events.DRIVER_REACH, rideCancelled())
        .build()
    );
  }

  public Consumer<DeferredResult> errorResultSetter(States state, Events event) {
    return Optional.ofNullable(MAPPING.get(state)).map(e -> e.get(event)).orElse(completeWithoutError());
  }

  private static Consumer<DeferredResult> rideCompleted() {
    return errorMessage("Ride is already completed");
  }

  private static Consumer<DeferredResult> rideCancelled() {
    return errorMessage("Ride is already cancelled");
  }

  private static Consumer<DeferredResult> completeWithoutError() {
    return d -> d.setResult(null);
  }

  private static Consumer<DeferredResult> errorMessage(String message) {
    return d -> d.setErrorResult(new BadRequestException(message));
  }

  private static Consumer<DeferredResult> rideIsInProgress() {
    return errorMessage("You can't cancel the ride which is in progress");
  }

  private static Consumer<DeferredResult> rideAlreadyDeclined() {
    return errorMessage("Ride is already accepted, you can't decline it");
  }

  private static Consumer<DeferredResult> rideAlreadyAccepted() {
    return errorMessage("Ride is already accepted, you can't accept it");
  }
}
