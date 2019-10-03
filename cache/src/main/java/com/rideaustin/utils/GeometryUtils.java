package com.rideaustin.utils;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.google.common.collect.Lists;
import com.google.maps.model.LatLng;
import com.rideaustin.model.Area;
import com.rideaustin.model.AreaExclusion;
import com.rideaustin.model.surgepricing.AreaGeometry;
import com.rideaustin.service.model.BoundingBox;
import com.sromku.polygon.Point;
import com.sromku.polygon.Polygon;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GeometryUtils {

  private GeometryUtils() {
  }

  public static BoundingBox getBoundingBox(List<LatLng> coordinates) {
    if (CollectionUtils.isEmpty(coordinates)) {
      return null;
    }
    double topLeftCornerLat = coordinates.get(0).lat;
    double topLeftCornerLng = coordinates.get(0).lng;
    double bottomRightCornerLat = coordinates.get(0).lat;
    double bottomRightCornerLng = coordinates.get(0).lng;
    for (LatLng coordinate : coordinates) {
      if (topLeftCornerLat > coordinate.lat) {
        topLeftCornerLat = coordinate.lat;
      }
      if (bottomRightCornerLat < coordinate.lat) {
        bottomRightCornerLat = coordinate.lat;
      }
      if (topLeftCornerLng < coordinate.lng) {
        topLeftCornerLng = coordinate.lng;
      }
      if (bottomRightCornerLng > coordinate.lng) {
        bottomRightCornerLng = coordinate.lng;
      }
    }
    return new BoundingBox(new LatLng(topLeftCornerLat, topLeftCornerLng), new LatLng(bottomRightCornerLat, bottomRightCornerLng));
  }

  public static void updatePolygon(AreaGeometry geo) {
    String[] points = geo.getCsvGeometry().split(" ");
    Polygon.Builder builder = new Polygon.Builder();
    for (String point : points) {
      String[] coords = point.split(",");
      builder.addVertex(new Point(Double.parseDouble(coords[1]), Double.parseDouble(coords[0])));
    }
    geo.setPolygon(buildPolygon(geo.getCsvGeometry()));
  }

  public static Polygon buildPolygon(String csvString) {
    String[] points = csvString.split(" ");
    Polygon.Builder builder = new Polygon.Builder();
    for (String point : points) {
      String[] coords = point.split(",");
      builder.addVertex(new Point(Double.parseDouble(coords[1]), Double.parseDouble(coords[0])));
    }
    return builder.build();
  }

  public static List<LatLng> buildCoordinates(String csvString) {
    String[] points = csvString.split(" ");
    List<LatLng> coordinates = Lists.newArrayList();
    for (String point : points) {
      String[] coords = point.split(",");
      coordinates.add(new LatLng(Double.parseDouble(coords[1]), Double.parseDouble(coords[0])));
    }
    return coordinates;
  }

  public static boolean isInsideArea(Area area, LatLng location) {
    updatePolygon(area.getAreaGeometry());
    boolean isWithinArea = area.getAreaGeometry().getPolygon().contains(location.lat, location.lng);
    if (!isWithinArea || CollectionUtils.isEmpty(area.getExclusions())) {
      return isWithinArea;
    } else {
      for (AreaExclusion exclusion : area.getExclusions()) {
        boolean isOnExcludedPart = exclusion.contains(location.lat, location.lng);
        if (isOnExcludedPart) {
          return false;
        }
      }
    }
    return true;
  }

  public static boolean isInsideExclusions(Area area, LatLng location) {
    if (CollectionUtils.isNotEmpty(area.getExclusions())) {
      for (AreaExclusion exclusion : area.getExclusions()) {
        boolean isOnExcludedPart = exclusion.contains(location.lat, location.lng);
        if (isOnExcludedPart) {
          return true;
        }
      }
    }
    return false;
  }

}
