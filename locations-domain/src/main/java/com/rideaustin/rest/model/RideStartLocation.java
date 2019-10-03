package com.rideaustin.rest.model;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class RideStartLocation implements RideLocation {

  @ApiModelProperty(required = true)
  @NotNull(message = "Could not find start location")
  private Double startLocationLat;
  @ApiModelProperty(required = true)
  @NotNull(message = "Could not find start location")
  private Double startLocationLong;
  @ApiModelProperty
  private String startAddress;
  @ApiModelProperty
  private String startZipCode;
  @ApiModelProperty
  private String startGooglePlaceId;

  @Override
  @ApiModelProperty(name = "startLocationLat", value = "Start location latitude", required = true, example = "30.286804")
  public Double getLat() {
    return startLocationLat;
  }

  @Override
  @ApiModelProperty(name = "startLocationLong", value = "Start location longitude", required = true, example = "-97.707425")
  public Double getLng() {
    return startLocationLong;
  }

  @Override
  @ApiModelProperty(name = "startAddress", value = "Start location address")
  public String getAddress() {
    return startAddress;
  }

  @Override
  @ApiModelProperty(name = "startZipCode", value = "Start location zipcode")
  public String getZipCode() {
    return startZipCode;
  }

  @Override
  @ApiModelProperty(name = "startGooglePlaceId", value = "Start location Google Place ID")
  public String getGooglePlaceId() {
    return startGooglePlaceId;
  }

  @JsonProperty("startLocationLat")
  public void setStartLocationLat(Double startLocationLat) {
    this.startLocationLat = startLocationLat;
  }

  @JsonProperty("startLocationLong")
  public void setStartLocationLong(Double startLocationLong) {
    this.startLocationLong = startLocationLong;
  }

  @JsonProperty("startAddress")
  public void setStartAddress(String startAddress) {
    this.startAddress = startAddress;
  }

  @JsonProperty("startZipCode")
  public void setStartZipCode(String startZipCode) {
    this.startZipCode = startZipCode;
  }

  @JsonProperty("startGooglePlaceId")
  public void setStartGooglePlaceId(String startGooglePlaceId) {
    this.startGooglePlaceId = startGooglePlaceId;
  }
}
