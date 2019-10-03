package com.rideaustin.service;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.model.Area;

import lombok.Getter;

@Getter
public class QueuedActiveDriverSearchCriteria extends BaseActiveDriverSearchCriteria {
  private final Area area;

  public QueuedActiveDriverSearchCriteria(Area area, List<Long> ignoreIds, String carCategory, Integer driverTypeBitmask) {
    this(area, ignoreIds, carCategory, driverTypeBitmask, null);
  }

  public QueuedActiveDriverSearchCriteria(Area area, List<Long> ignoreIds, String carCategory, Integer driverTypeBitmask,
    String directConnectId) {
    super(ignoreIds, carCategory, driverTypeBitmask, directConnectId == null ? Collections.emptyMap() : ImmutableMap.of("directConnectId", directConnectId));
    this.area = area;
  }
}
