package com.rideaustin.service.location.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LocationObject {

  private double latitude;
  private double longitude;
  private Double heading;
  private Double speed;
  private Double course;
  private Date locationUpdateDate;

  public LocationObject(double lat, double lng) {
    this.latitude = lat;
    this.longitude = lng;
  }

  public LocationObject(double latitude, double longitude, Double heading, Double speed, Double course) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.heading = heading;
    this.speed = speed;
    this.course = course;
    this.locationUpdateDate = new Date();
  }
}
