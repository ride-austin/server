package com.rideaustin.service.thirdparty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.LatLng;

@Component
public class MapsHelper {

  private static final double DISTANCE_DIFF = 0.0002;
  static final double[] ACCURACY_DECREMENT = new double[]{DISTANCE_DIFF, 0.0005, 0.0009, 0.002, 0.005, 0.009, 0.02, 0.05, 0.09, 0.2};

  public List<LatLng> toPoints(DirectionsStep s) {
    List<LatLng> result = new ArrayList<>();
    if (s != null) {
      addIfNotNull(result, Collections.singletonList(s.startLocation));
      if (s.polyline != null) {
        addIfNotNull(result, s.polyline.decodePath());
      }
      addIfNotNull(result, Collections.singletonList(s.endLocation));
    }
    return result;
  }

  public List<LatLng> reduce(List<LatLng> path) {
    return reduce(path, DISTANCE_DIFF);
  }

  public List<LatLng> reduce(List<LatLng> path, double accuracy) {
    if (path != null) {
      path = new LinkedList<>(path);
      boolean reducable = true;
      while (path.size() > 2 && reducable) {
        reducable = tryToReduce(path, accuracy);
      }
    }
    return path;
  }

  public String polylineEncodedPath(List<LatLng> points) {
    return new EncodedPolyline(points).getEncodedPath();
  }

  private void addIfNotNull(List<LatLng> result, List<LatLng> locations) {
    if (locations != null) {
      result.addAll(locations);
    }
  }

  private boolean tryToReduce(List<LatLng> path, double accuracy) {
    int reductionsCount = 0;
    for (int i = 0; i < path.size() - 2; i++) {
      if (isMiddlePointIrrelevant(path.get(i), path.get(i + 1), path.get(i + 2), accuracy)) {
        path.remove(i + 1);
        reductionsCount++;
      }
    }
    return reductionsCount > 0;
  }

  private boolean isMiddlePointIrrelevant(LatLng p1, LatLng p2, LatLng p3, double accuracy) {
    return distance(p1, p2) + distance(p2, p3) < distance(p1, p3) + accuracy;
  }

  private double distance(LatLng p1, LatLng p2) {
    return Math.sqrt(Math.pow(p1.lat - p2.lat, 2) + Math.pow(p1.lng - p2.lng, 2));
  }
}
