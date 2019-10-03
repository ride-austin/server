package com.rideaustin.utils.map;

import static com.rideaustin.Constants.EARTH_RADIUS;
import static com.rideaustin.Constants.TO_RADIANS;

import java.util.Comparator;
import java.util.List;

import javax.annotation.Nonnull;

import com.rideaustin.model.DistanceAware;
import com.rideaustin.model.LocationAware;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MapUtils {

  private MapUtils(){}

  public static double calculateDirectDistance(double fromLong, double fromLat, double toLong, double toLat) {
    double dLong = (toLong - fromLong) * TO_RADIANS;
    double dLat = (toLat - fromLat) * TO_RADIANS;

    double a = Math.pow(Math.sin(dLat / 2.0), 2)
      + Math.cos(fromLat * TO_RADIANS)
      * Math.cos(toLat * TO_RADIANS)
      * Math.pow(Math.sin(dLong / 2.0), 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    double d = EARTH_RADIUS * c;
    log.debug(String.format("Haversine distance between %s,%s and %s,%s=%s", fromLat, fromLong, toLat, toLong, d));
    return d;
  }

  public static <T extends DistanceAware & LocationAware> void updateDistanceToRider(@Nonnull Double latitude, @Nonnull Double longitude, List<T> items) {
    items.forEach(item ->
      item.setDirectDistanceToRider(calculateDirectDistance(longitude, latitude, item.getLongitude(), item.getLatitude()))
    );
    items.sort(Comparator.comparing(DistanceAware::getDirectDistanceToRider));
  }
}