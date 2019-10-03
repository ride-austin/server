package com.rideaustin.service.event.model;

import java.util.List;

import lombok.Getter;

@Getter
public class SurgeAreasUpdateEvent {

  private final List<SurgeAreaUpdateContent> surgeAreas;

  public SurgeAreasUpdateEvent(List<SurgeAreaUpdateContent> surgeAreas) {
    this.surgeAreas = surgeAreas;
  }
}
