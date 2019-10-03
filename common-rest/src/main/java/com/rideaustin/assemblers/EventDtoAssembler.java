package com.rideaustin.assemblers;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.rideaustin.model.Event;
import com.rideaustin.rest.model.EventDto;

@Component
public class EventDtoAssembler implements SingleSideAssembler<Event, EventDto> {

  @Override
  public EventDto toDto(Event event) {
    EventDto eventDto = new EventDto();
    if (event == null) {
      return null;
    }
    BeanUtils.copyProperties(event, eventDto, "ride");
    return eventDto;
  }

}
