package com.rideaustin.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class RideEndLocation implements RideLocation {

  @ApiModelProperty
  private Double endLocationLat;
  @ApiModelProperty
  private Double endLocationLong;
  @ApiModelProperty
  private String endAddress;
  @ApiModelProperty
  private String endZipCode;
  @ApiModelProperty
  private String endGooglePlaceId;

  @Override
  @ApiModelProperty(name = "endLocationLat", value = "End location latitude", required = true, example = "30.286804")
  public Double getLat() {
    return endLocationLat;
  }

  @Override
  @ApiModelProperty(name = "endLocationLong", value = "End location longitude", required = true, example = "-97.707425")
  public Double getLng() {
    return endLocationLong;
  }

  @Override
  @ApiModelProperty(name = "endAddress", value = "End location address")
  public String getAddress() {
    return endAddress;
  }

  @Override
  @ApiModelProperty(name = "endZipCode", value = "End location zipcode")
  public String getZipCode() {
    return endZipCode;
  }

  @Override
  @ApiModelProperty(name = "endGooglePlaceId", value = "End location Google Place ID")
  public String getGooglePlaceId() {
    return endGooglePlaceId;
  }

  @JsonProperty("endLocationLat")
  public void setEndLocationLat(Double endLocationLat) {
    this.endLocationLat = endLocationLat;
  }

  @JsonProperty("endLocationLong")
  public void setEndLocationLong(Double endLocationLong) {
    this.endLocationLong = endLocationLong;
  }

  @JsonProperty("endAddress")
  public void setEndAddress(String endAddress) {
    this.endAddress = endAddress;
  }

  @JsonProperty("endZipCode")
  public void setEndZipCode(String endZipCode) {
    this.endZipCode = endZipCode;
  }

  @JsonProperty("endGooglePlaceId")
  public void setEndGooglePlaceId(String endGooglePlaceId) {
    this.endGooglePlaceId = endGooglePlaceId;
  }
}
