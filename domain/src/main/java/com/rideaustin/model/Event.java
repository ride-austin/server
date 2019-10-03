package com.rideaustin.model;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.EventType;
import com.rideaustin.model.ride.Ride;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Entity
@Getter
@Setter
@Table(name = "events")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Event {

  private static final Long ONE_WEEK = 7 * 24 * 3600 * 1000L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false)
  private Long id;

  @Column(name = "avatar_id")
  @JsonIgnore
  private Long avatarId;

  @Column(name = "avatar_type")
  @Enumerated(EnumType.STRING)
  @JsonIgnore
  private AvatarType avatarType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ride_id")
  private Ride ride;

  @Column(name = "created_on")
  @Temporal(TemporalType.TIMESTAMP)
  @JsonIgnore
  private Date createdOn;

  @Column(name = "expires_on")
  @Temporal(TemporalType.TIMESTAMP)
  @JsonIgnore
  private Date expiresOn;

  @Column(name = "event_type")
  @Enumerated(EnumType.STRING)
  private EventType eventType;

  @Column(name = "message")
  private String message;

  @Column(name = "parameters")
  private String parameters;

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Event) {
      return getId().equals(((Event) obj).getId());
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    if (getId() != null) {
      return getId().hashCode();
    }
    return super.hashCode();
  }

  public static Event create(Ride ride, Long avatarId, AvatarType avatarType, EventType eventType) {
    return create(ride, avatarId, avatarType, eventType, ONE_WEEK);
  }

  public static Event create(Ride ride, Long avatarId, AvatarType avatarType, EventType eventType, String message) {
    return create(ride, avatarId, avatarType, eventType, ONE_WEEK, message);
  }

  public static Event create(Ride ride, Long avatarId, AvatarType avatarType, EventType eventType, String message, String parameters) {
    return create(ride, avatarId, avatarType, eventType, ONE_WEEK, message, parameters);
  }

  public static Event create(Long avatarId, EventType eventType, String parameters) {
    return create(null, avatarId, AvatarType.DRIVER, eventType, ONE_WEEK, null, parameters);
  }

  public static Event create(Ride ride, Long avatarId, AvatarType avatarType, EventType eventType, Long expiresAfter) {
    return create(ride, avatarId, avatarType, eventType, expiresAfter, null);
  }

  private static Event create(Ride ride, Long avatarId, AvatarType avatarType, EventType eventType, Long expiresAfter, String message) {
    return create(ride, avatarId, avatarType, eventType, expiresAfter, message, null);
  }

  public static Event create(Ride ride, Long avatarId, AvatarType avatarType, EventType eventType, Long expiresAfter, String message, String parameters) {
    Event event = new Event();
    event.setAvatarId(avatarId);
    event.setAvatarType(avatarType);
    event.setCreatedOn(new Date());
    event.setEventType(eventType);
    event.setMessage(message);
    event.setParameters(parameters);
    event.setExpiresOn(new Date(System.currentTimeMillis() + expiresAfter));
    if (ride != null) {
      event.setRide(ride);
    }
    return event;
  }

  public Map<String, Object> getParameterObject(ObjectMapper mapper) {
    if (parameters == null) {
      return Collections.emptyMap();
    }
    try {
      return mapper.readValue(parameters, mapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class));
    } catch (IOException e) {
      log.error("Failed to get event parameters", e);
      return new HashMap<>();
    }
  }
}
