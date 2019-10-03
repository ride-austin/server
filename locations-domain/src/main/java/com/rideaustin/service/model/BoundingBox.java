package com.rideaustin.service.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class BoundingBox {

  private LatLng topLeftCorner;
  private LatLng bottomRightCorner;

  public BoundingBox(com.google.maps.model.LatLng topLeft, com.google.maps.model.LatLng bottomRight) {
    this.topLeftCorner = new LatLng(topLeft.lat, topLeft.lng);
    this.bottomRightCorner = new LatLng(bottomRight.lat, bottomRight.lng);
  }

  @Getter
  @Setter
  @ApiModel
  @AllArgsConstructor
  @NoArgsConstructor
  public static class LatLng {
    @ApiModelProperty(required = true, example = "30.286804")
    public double lat;
    @ApiModelProperty(required = true, example = "-97.707425")
    public double lng;
  }
}
