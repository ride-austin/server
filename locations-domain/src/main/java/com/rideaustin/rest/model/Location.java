package com.rideaustin.rest.model;

import com.google.maps.model.LatLng;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@ApiModel
@AllArgsConstructor
@NoArgsConstructor
public class Location {

  @ApiModelProperty(value = "Current GPS latitude", required = true, example = "30.286804")
  private Double lat;
  @ApiModelProperty(value = "Current GPS longitude", required = true, example = "-97.707425")
  private Double lng;

  public Location(LatLng latLng) {
    lat = latLng.lat;
    lng = latLng.lng;
  }

  public LatLng asLatLng() {
    return new LatLng(lat, lng);
  }
}
