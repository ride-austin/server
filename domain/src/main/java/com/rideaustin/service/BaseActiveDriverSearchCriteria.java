package com.rideaustin.service;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class BaseActiveDriverSearchCriteria {
  private final List<Long> ignoreIds;
  private final String carCategory;
  private final Integer driverTypeBitmask;
  private final Map<String, Object> extraParams;
}
