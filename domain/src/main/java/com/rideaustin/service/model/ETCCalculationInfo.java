package com.rideaustin.service.model;

import com.querydsl.core.annotations.QueryProjection;
import com.rideaustin.model.enums.RideStatus;

import lombok.Getter;

/**
 * Estimated time to completion - same as ETA but uses end coordinates instead of start ones
 */
@Getter
public class ETCCalculationInfo extends BaseEstimatedTimeCalculationInfo {

  private final long rideId;
  private final RideStatus status;
  private final double endLat;
  private final double endLng;

  @QueryProjection
  public ETCCalculationInfo(long activeDriverId, long rideId, RideStatus status, double endLat, double endLng) {
    super(activeDriverId);
    this.rideId = rideId;
    this.status = status;
    this.endLat = endLat;
    this.endLng = endLng;
  }
}
