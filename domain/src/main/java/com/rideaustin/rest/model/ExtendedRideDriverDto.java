package com.rideaustin.rest.model;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;

@Getter
public class ExtendedRideDriverDto {

  private final Long driverId;
  private final String lastName;
  private final String firstName;
  private final String phoneNumber;

  @QueryProjection
  public ExtendedRideDriverDto(Long driverId, String lastName, String firstName, String phoneNumber) {
    this.driverId = driverId;
    this.lastName = lastName;
    this.firstName = firstName;
    this.phoneNumber = phoneNumber;
  }
}
