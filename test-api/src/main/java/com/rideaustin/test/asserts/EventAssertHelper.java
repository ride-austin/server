package com.rideaustin.test.asserts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.rideaustin.model.Event;
import com.rideaustin.model.enums.EventType;
import com.rideaustin.model.user.Avatar;
import com.rideaustin.repo.jpa.EventRepository;

public class EventAssertHelper {

  @Inject
  private EventRepository eventRepository;

  public void assertLastEventIsSent(Avatar receiver, EventType eventType) {
    LinkedList<Event> events = eventRepository.findAll()
      .stream()
      .filter(e -> e.getAvatarId().equals(receiver.getId()))
      .collect(Collectors.toCollection(LinkedList::new));
    assertFalse("Expected to find at least 1 event", events.isEmpty());
    events.sort(Comparator.comparing(Event::getId));
    Event lastEvent = events.getLast();
    EventAssert.assertThat(lastEvent)
      .hasType(eventType)
      .hasTargetAvatar(receiver.getId())
      .hasTargetAvatarType(receiver.getType());
  }

  public void assertNoEventIsSent(Avatar receiver, EventType eventType) {
    LinkedList<Event> events = eventRepository.findAll()
      .stream()
      .filter(e -> e.getAvatarId().equals(receiver.getId()))
      .collect(Collectors.toCollection(LinkedList::new));

    if (!events.isEmpty()) {
      if (events.stream().anyMatch(e -> e.getEventType().equals(eventType))) {
        fail(String.format("Expected event %s not to be sent to avatar %d", eventType, receiver.getId()));
      }
    }
  }

  public void assertEventsAreSentWithType(int number, EventType eventType) {
    List<Event> events = eventRepository.findAll().stream().filter(e -> eventType.equals(e.getEventType())).collect(Collectors.toList());
    assertEquals(String.format("Expected to find %d event but was %d", number, events.size()), events.size(), number);
  }
}
