package com.rideaustin.assemblers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import com.rideaustin.model.Event;
import com.rideaustin.model.enums.EventType;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.rest.model.EventDto;

public class EventDtoAssemblerTest {

  private EventDtoAssembler testedInstance;

  @Before
  public void setUp() throws Exception {
    testedInstance = new EventDtoAssembler();
  }

  @Test
  public void toDtoReplacesNull() {
    final EventDto result = testedInstance.toDto((Event) null);

    assertNull(result);
  }

  @Test
  public void toDtoFillsInfo() {
    final Event event = createEvent();

    final EventDto result = testedInstance.toDto(event);

    assertEquals(event.getId(), result.getId());
    assertEquals(event.getEventType(), result.getEventType());
    assertEquals(event.getMessage(), result.getMessage());
    assertEquals(event.getParameters(), result.getParameters());
  }

  @Test
  public void toDtoSkipsRide() {
    final Event event = createEvent();
    event.setRide(new Ride());

    final EventDto result = testedInstance.toDto(event);

    assertNull(result.getRide());
  }

  private Event createEvent() {
    final Event event = new Event();
    event.setId(1L);
    event.setEventType(EventType.QUEUED_AREA_ENTERING);
    event.setMessage("A");
    event.setParameters("B");
    return event;
  }
}