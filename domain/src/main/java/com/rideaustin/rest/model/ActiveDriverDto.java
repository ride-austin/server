package com.rideaustin.rest.model;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
public class ActiveDriverDto {

  private final double latitude;
  private final double longitude;
  @Setter
  private Set<String> carCategories;
  @JsonIgnore
  private final int availableCarCategories;
  private final DriverDto driver;
  @Setter
  private String appVersion;
  @JsonIgnore
  private final long userId;

  public ActiveDriverDto(double latitude, double longitude, int availableCarCategories, long driverId, String fullName, String phoneNumber, long userId) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.availableCarCategories = availableCarCategories;
    this.userId = userId;
    this.driver = new DriverDto(driverId, fullName, phoneNumber);
  }

  @Getter
  @RequiredArgsConstructor
  private static class DriverDto {
    private final long id;
    private final String fullName;
    private final String phoneNumber;
  }
}