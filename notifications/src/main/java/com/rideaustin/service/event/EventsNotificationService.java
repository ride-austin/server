package com.rideaustin.service.event;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.rideaustin.model.Event;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.EventType;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.jpa.EventRepository;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.model.DispatchCandidate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Primary
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class EventsNotificationService {

  static final String UNABLE_TO_SEND_EVENT_MESSAGE = "Unable to send event";
  protected final EventRepository eventRepository;
  private final ObjectMapper objectMapper;

  public void sendRideRequest(Ride ride, DispatchCandidate candidate, long expiryPeriod, Date acceptanceExpiration, Date allowanceExpiration, Long eta) {
    log.info("Ride dispatch is stored as event for ride id {} and to active driver id {}", ride.getId(), candidate.getId());
    eventRepository.save(Event.create(ride, candidate.getDriverId(), AvatarType.DRIVER, EventType.REQUESTED, expiryPeriod, null, createRideRequestEventConfigParameters(ride.getId(), acceptanceExpiration, allowanceExpiration, eta, candidate.isStacked())));
  }

  public void sendHandshakeRequest(long rideId, DispatchCandidate candidate, long expiration, Date allowance) {
    log.info("Dispatch handshake is stored as event for active driver id {}", candidate.getId());
    String handshakeParams = null;
    try {
      ImmutableMap<String, Object> paramsMap = ImmutableMap.of(
        "handshakeExpiration", allowance,
        "rideId", rideId
      );
      handshakeParams = objectMapper.writeValueAsString(paramsMap);
    } catch (JsonProcessingException e) {
      log.error(e.getMessage(), e);
    }
    eventRepository.save(Event.create(null, candidate.getDriverId(), AvatarType.DRIVER, EventType.HANDSHAKE,
      expiration, null, handshakeParams));
  }

  public void sendRideUpdateToDriver(Ride ride, DispatchCandidate driver, EventType eventType) {
    eventRepository.save(Event.create(ride, driver.getDriverId(), AvatarType.DRIVER, eventType));
  }

  public void sendGoOfflineToDriver(Long driverId, Map<String, String> parameters) throws ServerError {
    try {
      eventRepository.save(Event.create(driverId, EventType.GO_OFFLINE, objectMapper.writeValueAsString(parameters)));
    } catch (JsonProcessingException e) {
      throw new ServerError(UNABLE_TO_SEND_EVENT_MESSAGE, e);
    }
  }

  public void sendQueuedAreaEnteringToDriver(long driverId, Map<String, String> parameters) throws ServerError {
    try {
      eventRepository.save(Event.create(driverId, EventType.QUEUED_AREA_ENTERING, objectMapper.writeValueAsString(parameters)));
    } catch (JsonProcessingException e) {
      throw new ServerError(UNABLE_TO_SEND_EVENT_MESSAGE, e);
    }
  }

  public void sendQueuedAreaLeavingToDriver(long driverId, Map<String, String> parameters) throws ServerError {
    try {
      EventType eventType = EventType.QUEUED_AREA_LEAVING;
      eventRepository.save(Event.create(driverId, eventType, objectMapper.writeValueAsString(buildMessageParameters(eventType, parameters))));
    } catch (JsonProcessingException e) {
      throw new ServerError(UNABLE_TO_SEND_EVENT_MESSAGE, e);
    }
  }

  public void sendQueuedAreaGoingInactiveToDriver(long driverId, Map<String, String> parameters) throws ServerError {
    try {
      EventType eventType = EventType.QUEUED_AREA_LEAVING_INACTIVE;
      eventRepository.save(Event.create(driverId, eventType, objectMapper.writeValueAsString(buildMessageParameters(eventType, parameters))));
    } catch (JsonProcessingException e) {
      throw new ServerError(UNABLE_TO_SEND_EVENT_MESSAGE, e);
    }
  }

  public void sendQueuedAreaPenalizedToDriver(long driverId, Map<String, String> parameters) throws ServerError {
    try {
      EventType eventType = EventType.QUEUED_AREA_LEAVING_PENALTY;
      eventRepository.save(Event.create(driverId, eventType, objectMapper.writeValueAsString(buildMessageParameters(eventType, parameters))));
    } catch (JsonProcessingException e) {
      throw new ServerError(UNABLE_TO_SEND_EVENT_MESSAGE, e);
    }
  }

  public void sendQueuedAreaTakingRideToDriver(long driverId, Map<String, String> parameters) throws ServerError {
    try {
      eventRepository.save(Event.create(driverId, EventType.QUEUED_AREA_LEAVING_RIDE, objectMapper.writeValueAsString(parameters)));
    } catch (JsonProcessingException e) {
      throw new ServerError(UNABLE_TO_SEND_EVENT_MESSAGE, e);
    }
  }

  public void sendQueuedAreaUpdateToDriver(long driverId, Map<String, String> parameters) throws ServerError {
    try {
      eventRepository.save(Event.create(driverId, EventType.QUEUED_AREA_UPDATE, objectMapper.writeValueAsString(parameters)));
    } catch (JsonProcessingException e) {
      throw new ServerError(UNABLE_TO_SEND_EVENT_MESSAGE, e);
    }
  }

  public void sendRatingUpdated(long driverId, Double rating) {
    eventRepository.save(Event.create(null, driverId, AvatarType.DRIVER, EventType.RATING_UPDATED, String.valueOf(rating)));
  }

  public void sendCarCategoryChange(long driverId) throws ServerError {
    try {
      ImmutableMap.Builder<String, Object> paramsBuilder = ImmutableMap.builder();
      paramsBuilder.put("source", "ADMIN_EDIT");
      eventRepository.save(Event.create(driverId, EventType.CAR_CATEGORY_CHANGE, objectMapper.writeValueAsString(paramsBuilder.build())));
    } catch (JsonProcessingException e) {
      throw new ServerError(UNABLE_TO_SEND_EVENT_MESSAGE, e);
    }
  }

  public void sendCarCategoryChange(long driverId, Collection<String> disabledCategories) throws ServerError {
    try {
      ImmutableMap.Builder<String, Object> paramsBuilder = ImmutableMap.builder();
      paramsBuilder.put("source", "MISSED_REQUEST");
      paramsBuilder.put("disabled", disabledCategories);
      eventRepository.save(Event.create(driverId, EventType.CAR_CATEGORY_CHANGE, objectMapper.writeValueAsString(paramsBuilder.build())));
    } catch (JsonProcessingException e) {
      throw new ServerError(UNABLE_TO_SEND_EVENT_MESSAGE, e);
    }
  }

  public void sendDriverTypeChange(Driver driver) {
    eventRepository.save(Event.create(null, driver.getId(), driver.getType(), EventType.DRIVER_TYPE_UPDATE));
  }

  public void sendRiderLocationUpdate(long driverId, Double latitude, Double longitude, Long currentTimestamp) {
    try {
      eventRepository.save(Event.create(driverId, EventType.RIDER_LOCATION_UPDATED, objectMapper.writeValueAsString(
        ImmutableMap.of(
          "lat", latitude,
          "lng", longitude,
          "timeRecorded", currentTimestamp
        )
      )));
    } catch (JsonProcessingException e) {
      log.error(UNABLE_TO_SEND_EVENT_MESSAGE, e);
    }
  }

  public void sendRideUpgradeAccepted(Ride ride, long driverId) {
    eventRepository.save(Event.create(ride, driverId, AvatarType.DRIVER, EventType.RIDE_UPGRADE_ACCEPTED, 60000L));
  }

  public void sendRideUpgradeDeclined(long rideId, long driverId) {
    try {
      eventRepository.save(Event.create(null, driverId, AvatarType.DRIVER, EventType.RIDE_UPGRADE_DECLINED, 60000L, "", objectMapper.writeValueAsString(
        ImmutableMap.of(
          "rideId", rideId
        )
      )));
    } catch (JsonProcessingException e) {
      log.error(UNABLE_TO_SEND_EVENT_MESSAGE, e);
    }
  }

  public void sendConfigurationItemChangedEvent(long avatarId, AvatarType avatarType, Long configurationItemId, String configurationKey, String oldValue, String newValue) {
    try {
      long fiveHours = 5 * 60 * 60 * 1000L;
      eventRepository.save(Event.create(null, avatarId, avatarType, EventType.CONFIG_CHANGED, fiveHours, null, objectMapper.writeValueAsString(
        ImmutableMap.of(
          "oldValue", oldValue,
          "newValue", newValue,
          "id", configurationItemId,
          "key", configurationKey
        )
      )));
    } catch (JsonProcessingException e) {
      log.error(UNABLE_TO_SEND_EVENT_MESSAGE, e);
    }
  }

  public void sendConfigurationItemCreatedEvent(long avatarId, AvatarType avatarType, Long configurationItemId, String configurationKey, String value) {
    try {
      long fiveHours = 5 * 60 * 60 * 1000L;
      eventRepository.save(Event.create(null, avatarId, avatarType, EventType.CONFIG_CREATED, fiveHours, null, objectMapper.writeValueAsString(
        ImmutableMap.of(
          "value", value,
          "id", configurationItemId,
          "key", configurationKey
        )
      )));
    } catch (JsonProcessingException e) {
      log.error(UNABLE_TO_SEND_EVENT_MESSAGE, e);
    }
  }

  private String createRideRequestEventConfigParameters(long rideId, Date acceptanceExpiration, Date allowanceExpiration, Long eta, boolean stacked) {
    String parameters = null;
    try {
      parameters = objectMapper.writeValueAsString(
        ImmutableMap.of(
          "acceptanceExpiration", acceptanceExpiration,
          "acknowledgeExpiration", allowanceExpiration,
          "eta", eta,
          "stacked", stacked
        )
      );
    } catch (Exception e) {
      log.error(String.format("Error writing ride request dispatch configuration for ride id %d", rideId), e);
    }
    return parameters;
  }

  private Map<String, String> buildMessageParameters(EventType eventType, Map<String, String> parameters) {
    return ImmutableMap.<String, String>builder()
      .putAll(parameters)
      .put("message", eventType.getMessage())
      .build();
  }
}
