package com.rideaustin.service.model;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;

@Getter
public class DispatchRequest {
  private final long id;
  private final long activeDriverId;
  private final long driverId;

  @QueryProjection
  public DispatchRequest(long id, long activeDriverId, long driverId) {
    this.id = id;
    this.activeDriverId = activeDriverId;
    this.driverId = driverId;
  }
}
