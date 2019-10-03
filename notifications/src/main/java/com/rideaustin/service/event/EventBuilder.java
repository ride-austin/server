package com.rideaustin.service.event;

import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.model.Event;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.EventType;
import com.rideaustin.model.ride.Ride;

public class EventBuilder {

  private static final Long ONE_WEEK = 7 * 24 * 3600 * 1000L;
  private static final ObjectMapper mapper = new ObjectMapper();
  private Event event;

  private EventBuilder(Event event) {
    this.event = event;
  }

  public static EventBuilder create(Long avatarId, AvatarType avatarType, EventType eventType) {
    Event event = new Event();
    event.setAvatarId(avatarId);
    event.setAvatarType(avatarType);
    event.setEventType(eventType);
    event.setCreatedOn(new Date());
    event.setExpiresOn(new Date(System.currentTimeMillis() + ONE_WEEK));
    return new EventBuilder(event);
  }

  public EventBuilder setExpirationPeriod(Long period) {
    event.setExpiresOn(new Date(System.currentTimeMillis() + period));
    return this;
  }

  public EventBuilder setRide(Ride ride) {
    event.setRide(ride);
    return this;
  }

  public EventBuilder setMessage(String message) {
    event.setMessage(message);
    return this;
  }

  public EventBuilder setParameters(Map<Object, Object> parametersMap) {
    try {
      event.setParameters(mapper.writeValueAsString(parametersMap));
    } catch (JsonProcessingException e) {
      throw new EventCreationException(e);
    }
    return this;
  }

  public EventBuilder setParameters(String parameters) {
    event.setParameters(parameters);
    return this;
  }

  public EventBuilder setParameters(Object parametersObject) {
    try {
      event.setParameters(mapper.writeValueAsString(parametersObject));
    } catch (JsonProcessingException e) {
      throw new EventCreationException(e);
    }
    return this;
  }

  public Event get() {
    return event;
  }

  private static class EventCreationException extends RuntimeException {
    EventCreationException(Exception cause) {
      super(cause);
    }
  }
}