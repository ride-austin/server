package com.rideaustin.service.location.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rideaustin.model.LocationAware;

import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RiderLocation implements LocationAware {

  private final long id;
  private final double latitude;
  private final double longitude;
  private Date locationUpdateDate;

  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public RiderLocation(@JsonProperty("id") long id, @JsonProperty("latitude") double latitude,
    @JsonProperty("longitude") double longitude) {
    this.id = id;
    this.latitude = latitude;
    this.longitude = longitude;
    this.locationUpdateDate = new Date();
  }

  @Override
  @JsonIgnore
  public LocationObject getLocationObject() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setLocationObject(LocationObject locationObject) {
    throw new UnsupportedOperationException();
  }

  public Date getLocationUpdateDate() {
    return locationUpdateDate;
  }

  public void setLocationUpdateDate(Date locationUpdateDate) {
    this.locationUpdateDate = locationUpdateDate;
  }
}
