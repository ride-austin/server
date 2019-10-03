package com.rideaustin.rest.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.maps.model.LatLng;
import com.rideaustin.model.Area;
import com.rideaustin.model.AreaExclusion;
import com.rideaustin.service.model.BoundingBox;
import com.rideaustin.utils.GeometryUtils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class AreaQueuePositions {

  @ApiModelProperty(required = true)
  private String areaQueueName;
  @ApiModelProperty(required = true)
  private String iconUrl;
  @ApiModelProperty(required = true)
  private MapDisplayConfig mapDisplayConfig;

  @ApiModelProperty(required = true)
  private Map<String, Integer> positions = new HashMap<>();
  @ApiModelProperty(required = true)
  private Map<String, Integer> lengths = new HashMap<>();

  public AreaQueuePositions() {}

  public AreaQueuePositions(Area area, Map<String, Integer> lengths) {
    this.areaQueueName = area.getName();
    this.iconUrl = area.getIconUrl();
    this.lengths = lengths;
    this.mapDisplayConfig = new MapDisplayConfig(area);
  }

  public AreaQueuePositions withDisplayConfigFor(Area area) {
    this.mapDisplayConfig = new MapDisplayConfig(area);
    return this;
  }

  @Getter
  @ApiModel
  @JsonIgnoreProperties(ignoreUnknown = true)
  private static class MapDisplayConfig {

    @JsonIgnore
    @ApiModelProperty(hidden = true)
    private final Area area;

    MapDisplayConfig(Area area) {
      this.area = area;
    }

    @JsonProperty
    @ApiModelProperty(required = true)
    public boolean isEnabled() {
      return area.isEnabled();
    }

    @JsonProperty
    @ApiModelProperty(required = true)
    public String getIconUrl() {
      return area.getMapIconUrl();
    }

    @JsonProperty
    @ApiModelProperty(required = true)
    public BoundingBox.LatLng getIconCoordinate() {
      final String[] split = Optional.ofNullable(area.getMapIconCoords())
        .orElse("")
        .split(",");
      if (split.length < 2) {
        return new BoundingBox.LatLng();
      }
      return new BoundingBox.LatLng(Double.valueOf(split[0]), Double.valueOf(split[1]));
    }

    @JsonProperty
    @ApiModelProperty(required = true)
    public List<AreaDto> getExclusionAreas() {
      return area.getExclusions().stream()
        .filter(AreaExclusion::isLeaveAreaOnEnter)
        .map(AreaDto::new)
        .collect(Collectors.toList());
    }

    @ApiModelProperty(required = true)
    public List<AreaDto> getWaitingAreas() {
      return Collections.singletonList(new AreaDto(area));
    }

    @Getter
    @ApiModel
    private static class AreaDto {

      @ApiModelProperty(required = true)
      private final String name;
      @ApiModelProperty(required = true)
      private final List<LatLng> areaPolygon;

      AreaDto(AreaExclusion exclusion) {
        this.name = exclusion.getAreaGeometry().getName();
        this.areaPolygon = GeometryUtils.buildCoordinates(exclusion.getAreaGeometry().getCsvGeometry());
      }

      AreaDto(Area area) {
        this.name = area.getName();
        if (area.getAreaGeometry() != null) {
          this.areaPolygon = GeometryUtils.buildCoordinates(area.getAreaGeometry().getCsvGeometry());
        } else {
          this.areaPolygon = Collections.emptyList();
        }
      }
    }
  }
}
