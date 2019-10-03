package com.rideaustin.test.asserts;

import java.util.Objects;

import org.assertj.core.api.AbstractAssert;

import com.rideaustin.model.Event;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.EventType;

public class EventAssert extends AbstractAssert<EventAssert, Event> {

  private EventAssert(Event event) {
    super(event, EventAssert.class);
  }

  public static EventAssert assertThat(Event event) {
    return new EventAssert(event);
  }

  public EventAssert hasType(EventType eventType) {
    isNotNull();
    if (!Objects.equals(actual.getEventType(), eventType)) {
      failWithMessage("Expected event type <%s> but was <%s>", eventType, actual.getEventType());
    }
    return this;
  }

  public EventAssert hasTargetAvatarType(AvatarType avatarType) {
    isNotNull();
    if (!Objects.equals(actual.getAvatarType(), avatarType)) {
      failWithMessage("Expected avatar type <%s> but was <%s>", avatarType, actual.getEventType());
    }
    return this;
  }

  public EventAssert hasTargetAvatar(Long avatarId) {
    isNotNull();
    if (!Objects.equals(actual.getAvatarId(), avatarId)) {
      failWithMessage("Expected avatar <%s> but was <%s>", avatarId, actual.getEventType());
    }
    return this;
  }

}
