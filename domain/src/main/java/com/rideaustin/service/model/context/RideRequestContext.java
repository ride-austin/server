package com.rideaustin.service.model.context;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RideRequestContext {

  private Long rideId;
  private Long riderId;
  private Double startLocationLat;
  private Double startLocationLong;
  private Long cityId;
  private String requestedCarTypeCategory;
  private Integer requestedCarTypeBitmask;
  private Set<Long> ignoreIds;
  private int driverSearchRadius;
  private Integer requestedDriverTypeBitmask;
  private String directConnectId;
  private Date createdDate;
  private String applePayToken;

  public RideRequestContext() {
    this.createdDate = new Date();
  }

  public RideRequestContext(Long rideId, Long riderId, Double startLocationLat, Double startLocationLong, Long cityId,
    String requestedCarTypeCategory, Integer requestedCarTypeBitmask, List<Long> ignoreIds, int driverSearchRadius,
    String applePayToken) {
    this.rideId = rideId;
    this.riderId = riderId;
    this.startLocationLat = startLocationLat;
    this.startLocationLong = startLocationLong;
    this.cityId = cityId;
    this.requestedCarTypeCategory = requestedCarTypeCategory;
    this.requestedCarTypeBitmask = requestedCarTypeBitmask;
    this.ignoreIds = new HashSet<>(ignoreIds);
    this.driverSearchRadius = driverSearchRadius;
    this.applePayToken = applePayToken;
    this.createdDate = new Date();
  }

  @QueryProjection
  public RideRequestContext(Long rideId, Long riderId, Double startLocationLat, Double startLocationLong,
    Long cityId, String requestedCarTypeCategory, Integer requestedCarTypeBitmask, Integer requestedDriverTypeBitmask,
    Date requestedDate, String applePayToken) {
    this.rideId = rideId;
    this.riderId = riderId;
    this.startLocationLat = startLocationLat;
    this.startLocationLong = startLocationLong;
    this.cityId = cityId;
    this.requestedCarTypeCategory = requestedCarTypeCategory;
    this.requestedCarTypeBitmask = requestedCarTypeBitmask;
    this.ignoreIds = new HashSet<>();
    this.driverSearchRadius = 5;
    this.requestedDriverTypeBitmask = requestedDriverTypeBitmask;
    this.applePayToken = applePayToken;
    this.createdDate = requestedDate;
  }
}
