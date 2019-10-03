package com.rideaustin.model;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;
import lombok.Setter;

@Getter
public class MessagingRideInfoDTO {

  private final String riderPhoneNumber;
  private final String driverFirstName;
  private final String license;
  private final String color;
  private final String make;
  private final String model;
  private final long cityId;

  @Setter
  private long drivingTimeToRider;

  @QueryProjection
  public MessagingRideInfoDTO(String riderPhoneNumber, String driverFirstName, String license, String color, String make,
    String model, long cityId) {
    this.riderPhoneNumber = riderPhoneNumber;
    this.driverFirstName = driverFirstName;
    this.license = license;
    this.color = color;
    this.make = make;
    this.model = model;
    this.cityId = cityId;
  }
}
