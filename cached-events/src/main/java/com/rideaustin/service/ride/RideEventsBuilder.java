package com.rideaustin.service.ride;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.rest.model.RideEvents;
import com.rideaustin.service.ride.events.CachedEventType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RideEventsBuilder {

  private final RideDslRepository rideDslRepository;
  private final BeanFactory beanFactory;
  private final Integer backDateThreshold;

  @Inject
  public RideEventsBuilder(RideDslRepository rideDslRepository, BeanFactory beanFactory, Environment environment) {
    this.beanFactory = beanFactory;
    this.backDateThreshold = environment.getProperty("ride.events_backdate_threshold", Integer.class, 86400);
    this.rideDslRepository = rideDslRepository;
  }

  public List<RideEvent> buildEvents(RideEvents rideEvents) {
    List<RideEvent> events = new ArrayList<>();
    List<RideEventInfo> invalidEvents = new ArrayList<>();
    Set<Long> validRides = new HashSet<>();
    Set<Long> rideIds = rideEvents
      .getEvents()
      .stream()
      .map(m -> m.get("rideId"))
      .map(Long::valueOf)
      .collect(Collectors.toSet());
    Map<Long, RideStatus> statuses = rideDslRepository.getStatuses(rideIds);
    for (Map<String, String> eventProperties : rideEvents.getEvents()) {
      try {
        RideEvent event = buildEvent(eventProperties);
        ValidationResult validationResult = validate(event, statuses, validRides);
        if (validationResult.isValid()) {
          events.add(event);
        } else {
          invalidEvents.add(new RideEventInfo(event, validationResult));
        }
      } catch (Exception e) {
        log.error("Unknown event", e);
      }
    }
    if (!invalidEvents.isEmpty()) {
      log.error("Invalid events occurred: " + invalidEvents);
    }
    return events;
  }

  private RideEvent buildEvent(Map<String, String> rideEvent) throws ServerError {
    String event = rideEvent.get("eventType");
    if (event == null || !Arrays.stream(CachedEventType.values()).map(CachedEventType::name).collect(Collectors.toSet()).contains(event)) {
      String errorMessage = String.format("Unknown ride event %s", event);
      log.warn(errorMessage);
      throw new ServerError(errorMessage);
    } else {
      CachedEventType eventType = CachedEventType.valueOf(event);
      Class<? extends RideEvent> eventClass = eventType.getEventClass();
      return beanFactory.getBean(eventClass, rideEvent);
    }
  }

  private ValidationResult validate(RideEvent event, Map<Long, RideStatus> statuses, Set<Long> validRides) {
    if (validRides.contains(event.getRideId())) {
      return new ValidationResult(true);
    }
    final RideStatus status = statuses.get(event.getRideId());
    if (RideStatus.TERMINAL_STATUSES.contains(status)) {
      return new ValidationResult(false, ValidationResult.Reason.IN_TERMINAL_STATUS,
        String.format("Ride is in terminal status %s, further event processing will be skipped", status));
    } else {
      final ValidationResult timestampValidation = invalidTimestamp(event);
      if (!timestampValidation.isValid()) {
        return timestampValidation;
      }
    }
    validRides.add(event.getRideId());
    return new ValidationResult(true);
  }

  private ValidationResult invalidTimestamp(RideEvent event) {
    Instant eventTimestamp = Instant.ofEpochMilli(event.getTimestamp());
    boolean inFuture = eventTimestamp.isAfter(Instant.now());
    if (inFuture) {
      return new ValidationResult(false, ValidationResult.Reason.TIMESTAMP_IN_FUTURE,
        String.format("Event has timestamp %s which is in future", eventTimestamp));
    }
    boolean inPastBeforeThreshold = Duration.between(eventTimestamp, Instant.now()).getSeconds() > backDateThreshold;
    if (inPastBeforeThreshold) {
      return new ValidationResult(false, ValidationResult.Reason.TIMESTAMP_IN_PAST,
        String.format("Event has timestamp %s which is in past before threshold of %d seconds", eventTimestamp, backDateThreshold));
    }
    return new ValidationResult(true);
  }

  @Getter
  @RequiredArgsConstructor
  private static class RideEventInfo {
    private final RideEvent event;
    private final ValidationResult validationResult;

    @Override
    public String toString() {
      return String.format("{event=%s, validationResult=%s}", event, validationResult);
    }
  }

  @Getter
  @RequiredArgsConstructor
  private static class ValidationResult {
    enum Reason {
      IN_TERMINAL_STATUS,
      TIMESTAMP_IN_FUTURE,
      TIMESTAMP_IN_PAST
    }

    private final boolean valid;
    private final Reason reason;
    private final String message;

    ValidationResult(boolean valid) {
      this.valid = valid;
      this.reason = null;
      this.message = null;
    }

    @Override
    public String toString() {
      return valid ? "Valid event" : String.format("Invalid event{reason=%s, message='%s'}", reason, message);
    }
  }

}
