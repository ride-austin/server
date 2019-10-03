package com.rideaustin.service.model;

import java.util.Date;

import com.querydsl.core.annotations.QueryProjection;
import com.rideaustin.model.enums.ActiveDriverStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class DispatchCandidate {

  private long id;
  private long driverId;
  private String licensePlate;
  private long userId;
  private ActiveDriverStatus status;
  private double latitude;
  private double longitude;
  private Long drivingTimeToRider;
  private Long drivingDistanceToRider;
  private Date requestedAt;
  private boolean stacked;

  @QueryProjection
  public DispatchCandidate(long id, long driverId, String licensePlate, long userId, ActiveDriverStatus status) {
    this.id = id;
    this.driverId = driverId;
    this.userId = userId;
    this.licensePlate = licensePlate;
    this.status = status;
  }

  public void update(OnlineDriverDto ad) {
    setDrivingTimeToRider(ad.getDrivingTimeToRider());
    setDrivingDistanceToRider(ad.getDrivingDistanceToRider());
    setLatitude(ad.getLatitude());
    setLongitude(ad.getLongitude());
    setStacked(ad.isEligibleForStacking());
  }
}
