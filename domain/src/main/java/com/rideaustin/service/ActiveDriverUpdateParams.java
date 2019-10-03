package com.rideaustin.service;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ActiveDriverUpdateParams {

  private Double latitude;
  private Double longitude;
  private Double heading;
  private Double course;
  private Double speed;
  private Set<String> carCategories;
  private Set<String> driverTypes;
  private Long carId;
  private Long cityId;

  public ActiveDriverUpdateParams(Double latitude, Double longitude, Double heading, Double course, Double speed,
    Set<String> carCategories, Set<String> driverTypes, Long carId, Long cityId) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.heading = heading;
    this.course = course;
    this.speed = speed;
    this.carCategories = Optional.ofNullable(carCategories).orElse(Collections.emptySet());
    this.driverTypes = Optional.ofNullable(driverTypes).orElse(Collections.emptySet());
    this.carId = carId;
    this.cityId = cityId;
  }

}
