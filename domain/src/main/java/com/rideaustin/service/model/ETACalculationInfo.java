package com.rideaustin.service.model;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;

@Getter
public class ETACalculationInfo extends BaseEstimatedTimeCalculationInfo {

  private final double startLat;
  private final double startLng;

  @QueryProjection
  public ETACalculationInfo(long activeDriverId, double startLat, double startLng) {
    super(activeDriverId);
    this.startLat = startLat;
    this.startLng = startLng;
  }

}
