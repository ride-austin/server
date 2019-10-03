package com.rideaustin.service.thirdparty;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.joda.time.DateTime;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.google.maps.DirectionsApi;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.PlacesApi;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.AddressType;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;
import com.rideaustin.model.Address;
import com.rideaustin.rest.exception.ServerError;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GeoApiClientGoogleImpl implements GeoApiClient {

  public static final int MAP_SCALE = 2;
  private static final String STATIC_IMAGE_URL = "https://maps.googleapis.com/maps/api/staticmap";
  private static final int MAX_URL_CHARS = 2000;
  private final String staticMapApiKey;
  private final GeoApiContext geoApiContext;
  private final MapsHelper mapsHelper;

  @Inject
  public GeoApiClientGoogleImpl(Environment env, MapsHelper mapsHelper, GeoApiContext geoApiContext) {
    staticMapApiKey = env.getProperty("google.static.maps.key");
    this.geoApiContext = geoApiContext;
    this.mapsHelper = mapsHelper;

    // To access Google service in China. Please don't delete it. -- huang.jian@crossover.com
    try {
      if (StringUtils.equals("Huangs-MBP.lan", InetAddress.getLocalHost().getHostName())) {
        geoApiContext.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8118)));
      }
    } catch (UnknownHostException e) {
      log.error("Hostname can not be resolved", e);
    }
  }

  @Override
  public DistanceMatrix getDrivingDistance(LatLng destination, LatLng[] origins) {
    return DistanceMatrixApi.newRequest(geoApiContext)
      .mode(TravelMode.DRIVING)
      .origins(origins)
      .destinations(destination)
      .awaitIgnoreError();
  }

  @Override
  public DistanceMatrix getDrivingDistanceWithDelay(LatLng origin, LatLng destination, int minutes) throws ServerError {
    try {
      return DistanceMatrixApi.newRequest(geoApiContext)
        .mode(TravelMode.DRIVING).origins(origin).destinations(destination)
        .departureTime(DateTime.now().plusMinutes(minutes)).await();
    } catch (Exception e) {
      throw new ServerError(e);
    }
  }

  @Override
  public GeocodingResult[] geocodeAddress(Address address) throws ServerError {
    try {
      return GeocodingApi.geocode(geoApiContext, address.getAddress()).language("en").await();
    } catch (Exception e) {
      throw new ServerError(e);
    }
  }

  @Override
  public GeocodingResult[] reverseGeocode(double lat, double lng) throws ServerError {
    try {
      return GeocodingApi.reverseGeocode(geoApiContext, new LatLng(lat, lng))
        .resultType(AddressType.STREET_ADDRESS)
        .await();
    } catch (Exception e) {
      throw new ServerError(e);
    }
  }

  @Override
  public URI getMapUrl(@Nonnull List<LatLng> srcPoints, int scale) throws ServerError {
    try {
      List<LatLng> points = srcPoints;
      String polylineEncodedPath = mapsHelper.polylineEncodedPath(points);
      if (polylineEncodedPath.length() > MAX_URL_CHARS) {
        points = reducePath(points);
        polylineEncodedPath = mapsHelper.polylineEncodedPath(points);
      }
      return new URIBuilder(STATIC_IMAGE_URL)
        .addParameter("size", "640x441")
        .addParameter("scale", String.format("%d", scale))
        .addParameter("key", staticMapApiKey)
        .addParameter("markers", pointMarker("green", points.get(0)))
        .addParameter("markers", pointMarker("red", points.get(points.size() - 1)))
        .addParameter("path", "enc:" + polylineEncodedPath).build();
    } catch (URISyntaxException e) {
      throw new ServerError(e);
    }
  }

  @Override
  public List<LatLng> getGoogleMiddlePoints(LatLng from, LatLng to) {
    try {
      DirectionsResult directionsResult = DirectionsApi.newRequest(geoApiContext).mode(TravelMode.DRIVING).origin(from).destination(to).await();
      if (responsePresent(directionsResult)) {
        List<LatLng> pointsBetween = new ArrayList<>();
        pointsBetween.add(from);
        pointsBetween.addAll(Arrays.stream(directionsResult.routes[0].legs[0].steps)
          .map(mapsHelper::toPoints)
          .flatMap(Collection::stream)
          .collect(Collectors.toList()));
        pointsBetween.add(to);
        pointsBetween = mapsHelper.reduce(pointsBetween);
        pointsBetween.remove(0);
        pointsBetween.remove(pointsBetween.size() - 1);
        return pointsBetween;
      }
    } catch (Exception e) {
      log.warn("Could not get google DirectionAPI response while calculating middle points", e);
    }
    return new ArrayList<>();
  }

  @Override
  public AddressComponent[] retrieveAddress(String googlePlaceId) {
    return PlacesApi.placeDetails(geoApiContext, googlePlaceId).awaitIgnoreError().addressComponents;
  }

  private List<LatLng> reducePath(List<LatLng> points) {
    for (int i = 0; i < MapsHelper.ACCURACY_DECREMENT.length; i++) {
      points = mapsHelper.reduce(points, MapsHelper.ACCURACY_DECREMENT[i]);
      log.debug("Reduced to points.size: " + points.size());
      if (mapsHelper.polylineEncodedPath(points).length() <= MAX_URL_CHARS) {
        break;
      }
    }
    return points;
  }

  private boolean responsePresent(DirectionsResult resp) {
    return resp != null && resp.routes != null && resp.routes.length > 0 && resp.routes[0].legs != null
      && resp.routes[0].legs.length > 0 && resp.routes[0].legs[0].steps != null && resp.routes[0].legs[0].steps.length > 0;
  }

  @Nonnull
  private String pointMarker(@Nonnull String color, @Nonnull LatLng point) {
    return "color:" + color + "|" + point.lat + "," + point.lng;
  }
}
