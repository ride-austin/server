package com.rideaustin.events;

import com.rideaustin.model.enums.LostAndFoundRequestType;

import lombok.Getter;

@Getter
public class LostAndFoundTrackEvent {

  private final LostAndFoundRequestType type;
  private final Long requestedBy;
  private final String content;

  public LostAndFoundTrackEvent(LostAndFoundRequestType type, Long requestedBy, String content) {
    this.type = type;
    this.requestedBy = requestedBy;
    this.content = content;
  }
}
