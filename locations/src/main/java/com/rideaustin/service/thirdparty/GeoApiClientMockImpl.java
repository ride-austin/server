package com.rideaustin.service.thirdparty;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.AddressType;
import com.google.maps.model.Distance;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.DistanceMatrixElementStatus;
import com.google.maps.model.DistanceMatrixRow;
import com.google.maps.model.Duration;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.rideaustin.model.Address;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.utils.map.MapUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GeoApiClientMockImpl implements GeoApiClient {

  private static final String INITIAL_ADDRESS_MOCKED = "Initial Address Mocked";

  @Override
  public DistanceMatrix getDrivingDistance(LatLng destination, LatLng[] origins) {

    List<DistanceMatrixRow> rows = new ArrayList<>();
    for (LatLng origin : origins) {
      DistanceMatrixElement element = new DistanceMatrixElement();
      element.distance = new Distance();

      long directDistance;
      if (origin != null) {
        directDistance = (long) MapUtils.calculateDirectDistance(origin.lng, origin.lat, destination.lng, destination.lat);
      } else {
        directDistance = 1200L;
      }
      element.distance.inMeters = directDistance;
      element.distance.humanReadable = "1km 200m";
      element.duration = new Duration();
      element.duration.inSeconds = (long) (directDistance / 13.5);
      element.duration.humanReadable = "one minute";
      element.status = DistanceMatrixElementStatus.OK;

      DistanceMatrixRow row = new DistanceMatrixRow();
      row.elements = ImmutableList.of(element).toArray(new DistanceMatrixElement[0]);
      rows.add(row);
    }
    return new DistanceMatrix(new String[]{INITIAL_ADDRESS_MOCKED}, new String[]{INITIAL_ADDRESS_MOCKED}, rows.toArray(new DistanceMatrixRow[0]));
  }

  @Override
  public DistanceMatrix getDrivingDistanceWithDelay(LatLng origin, LatLng destination, int minutes) {
    DistanceMatrixElement element = new DistanceMatrixElement();
    element.distance = new Distance();
    long directDistance = (long) MapUtils.calculateDirectDistance(origin.lng, origin.lat, destination.lng, destination.lat);
    element.distance.inMeters = directDistance;
    element.distance.humanReadable = "1km 200m";
    element.duration = new Duration();
    element.duration.inSeconds = (long) (directDistance / 13.5);
    element.duration.humanReadable = "one minute";
    element.status = DistanceMatrixElementStatus.OK;

    log.info(String.format("Distance: %d; Time: %d", directDistance, element.duration.inSeconds));

    DistanceMatrixRow row = new DistanceMatrixRow();
    row.elements = ImmutableList.of(element).toArray(new DistanceMatrixElement[0]);

    return new DistanceMatrix(new String[]{INITIAL_ADDRESS_MOCKED}, new String[]{INITIAL_ADDRESS_MOCKED}, new DistanceMatrixRow[]{row});
  }

  @Override
  public GeocodingResult[] geocodeAddress(Address address) {
    return reverseGeocode(1d, 1d);
  }

  @Override
  public GeocodingResult[] reverseGeocode(double lat, double lng) {
    GeocodingResult result = new GeocodingResult();
    result.formattedAddress = "Austin Central Station";
    AddressComponent addressComponent = new AddressComponent();
    addressComponent.longName = "Austin Central Station";
    addressComponent.shortName = "Austin Central";
    addressComponent.types = new AddressComponentType[]{AddressComponentType.BUS_STATION, AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1};
    result.addressComponents = new AddressComponent[]{addressComponent};
    result.types = new AddressType[]{AddressType.SUBLOCALITY_LEVEL_1};
    return new GeocodingResult[]{result};
  }

  @Override
  public URI getMapUrl(@Nonnull List<LatLng> points, int scale) throws ServerError {
    try {
      return new URI("");

    } catch (URISyntaxException e) {
      throw new ServerError(e);
    }
  }

  @Override
  public List<LatLng> getGoogleMiddlePoints(LatLng from, LatLng to) {
    return new ArrayList<>();
  }

  @Override
  public AddressComponent[] retrieveAddress(String googlePlaceId) {
    return new AddressComponent[0];
  }
}
