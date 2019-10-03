package com.rideaustin.service;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableMap;

import lombok.Getter;

@Getter
public class ActiveDriverSearchCriteria extends BaseActiveDriverSearchCriteria {

  private final Double latitude;
  private final Double longitude;
  private final int updateTimeToDriveCount;
  private final Integer carCategoryBitmask;
  private final Long cityId;
  private final Integer searchRadius;
  private final Integer limit;

  public ActiveDriverSearchCriteria(Double latitude, Double longitude, List<Long> ignoreIds, int updateTimeToDriveCount,
    String carCategory, Integer carCategoryBitmask, Long cityId, Integer driverTypeBitmask, Integer searchRadius, Integer limit) {
    this(latitude, longitude, ignoreIds, updateTimeToDriveCount, carCategory, carCategoryBitmask, cityId, driverTypeBitmask,
      searchRadius, limit, null);
  }

  public ActiveDriverSearchCriteria(Double latitude, Double longitude, List<Long> ignoreIds, int updateTimeToDriveCount,
    String carCategory, Integer carCategoryBitmask, Long cityId, Integer driverTypeBitmask, Integer searchRadius, Integer limit,
    String directConnectId) {
    super(ignoreIds, carCategory, driverTypeBitmask, directConnectId == null ? Collections.emptyMap() : ImmutableMap.of("directConnectId", directConnectId));
    this.latitude = latitude;
    this.longitude = longitude;
    this.updateTimeToDriveCount = updateTimeToDriveCount;
    this.carCategoryBitmask = carCategoryBitmask;
    this.cityId = cityId;
    this.searchRadius = searchRadius;
    this.limit = limit;
  }

}
