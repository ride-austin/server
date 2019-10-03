package com.rideaustin.model.redis;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@ApiModel
public class AreaGeometry {

  @ApiModelProperty
  @JsonProperty("geometry_id")
  private Long id;
  @ApiModelProperty
  private Double topLeftCornerLat;
  @ApiModelProperty
  private Double topLeftCornerLng;
  @ApiModelProperty
  private Double bottomRightCornerLat;
  @ApiModelProperty
  private Double bottomRightCornerLng;
  @ApiModelProperty
  private Double centerPointLat;
  @ApiModelProperty
  private Double centerPointLng;
  @ApiModelProperty
  private String csvGeometry;
  @ApiModelProperty
  private Double labelLat;
  @ApiModelProperty
  private Double labelLng;

  public AreaGeometry(com.rideaustin.model.surgepricing.AreaGeometry areaGeometry) {
    this.id = areaGeometry.getId();
    this.topLeftCornerLat = areaGeometry.getTopLeftCornerLat();
    this.topLeftCornerLng = areaGeometry.getTopLeftCornerLng();
    this.bottomRightCornerLat = areaGeometry.getBottomRightCornerLat();
    this.bottomRightCornerLng = areaGeometry.getBottomRightCornerLng();
    this.centerPointLat = areaGeometry.getCenterPointLat();
    this.centerPointLng = areaGeometry.getCenterPointLng();
    this.csvGeometry = areaGeometry.getCsvGeometry();
    this.labelLat = areaGeometry.getLabelLat();
    this.labelLng = areaGeometry.getLabelLng();
  }

}
