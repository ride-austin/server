package com.rideaustin.utils.map;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rideaustin.service.model.BoundingBox.LatLng;
import com.sromku.polygon.Point;
import com.sromku.polygon.Polygon;

import lombok.Getter;
import lombok.Setter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class LocationCorrectorConfiguration {

  private final List<PickupHint> pickupHints;

  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public LocationCorrectorConfiguration(@JsonProperty("pickupHints") List<PickupHint> pickupHints) {
    this.pickupHints = pickupHints;
  }

  @Getter
  @Setter
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class PickupHint {

    private String name;
    @JsonProperty("areaPolygon")
    private List<LatLng> areaPolygonList;
    private List<DesignatedPickup> designatedPickups;
    @JsonIgnore
    private Polygon polygon;

    public Polygon getPolygon() {
      if (polygon == null) {
        Polygon.Builder builder = Polygon.Builder();
        for (LatLng point : areaPolygonList) {
          builder.addVertex(new Point(point.lat, point.lng));
        }
        polygon = builder.build();
      }
      return polygon;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DesignatedPickup {
      private String name;
      @JsonProperty("driverCoord")
      private LatLng point;
    }
  }
}
