package com.rideaustin.rest.model;

import java.util.List;
import java.util.Set;

import com.google.maps.model.LatLng;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@ApiModel
@AllArgsConstructor
public class CampaignDto {

  @ApiModelProperty(required = true)
  private final String headerIcon;
  @ApiModelProperty(required = true)
  private final String headerTitle;
  @ApiModelProperty(required = true)
  private final String body;
  @ApiModelProperty(required = true)
  private final String footer;
  @ApiModelProperty(required = true)
  private final Set<CampaignAreaDto> areas;


  @Getter
  @ApiModel
  @EqualsAndHashCode
  @AllArgsConstructor
  public static class CampaignAreaDto {
    @ApiModelProperty(required = true)
    private final String name;
    @ApiModelProperty(required = true)
    private final String color;
    @ApiModelProperty(required = true)
    private final List<LatLng> boundary;
  }
}
