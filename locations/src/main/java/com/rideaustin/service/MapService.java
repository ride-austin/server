package com.rideaustin.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import com.google.common.collect.ImmutableSet;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.rideaustin.config.CacheConfiguration;
import com.rideaustin.model.Address;
import com.rideaustin.model.DistanceAware;
import com.rideaustin.model.DrivingTimeAware;
import com.rideaustin.model.LocationAware;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.model.DistanceTime;
import com.rideaustin.service.thirdparty.GeoApiClientFactory;
import com.rideaustin.service.thirdparty.GeoApiClientGoogleImpl;
import com.rideaustin.service.thirdparty.RestTemplateFactory;
import com.rideaustin.utils.map.LocationCorrector;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Profile("!itest")
public class MapService {

  private static final String ERROR_MESSAGE = "Unable to calculate distance";

  private final RestTemplate restTemplate;
  private final GeoApiClientFactory geoApiClientFactory;
  private final LocationCorrector locationCorrector;
  private final long expectationTime;

  @Inject
  public MapService(Environment env, GeoApiClientFactory geoApiClientFactory,
    RestTemplateFactory restTemplateFactory, LocationCorrector locationCorrector) {

    expectationTime = env.getProperty("dispatch.addSecondsToDrivingTime", Long.class, 0L);

    this.geoApiClientFactory = geoApiClientFactory;
    restTemplate = restTemplateFactory.get();
    this.locationCorrector = locationCorrector;
  }

  public List<LatLng> getGooglePointsBetween(double fromLat, double fromLong, double toLat, double toLong) {
    return geoApiClientFactory.createGeoApiClient().getGoogleMiddlePoints(new LatLng(fromLat, fromLong), new LatLng(toLat, toLong));
  }

  @Cacheable(cacheNames = CacheConfiguration.ESTIMATION_CACHE, keyGenerator = "estimationKeyGenerator")
  public DistanceTime computeDistanceTime(LatLng origin, LatLng destination) throws MapException {
    DistanceMatrix matrix;
    try {
      // set departure time to 10 minutes later - average time for the driver
      // to pick up a rider
      matrix = geoApiClientFactory.createGeoApiClient().getDrivingDistanceWithDelay(origin, destination, 10);
      return processDistanceMatrixElement(matrix.rows[0].elements[0]);
    } catch (Exception e) {
      throw new MapException(ERROR_MESSAGE, e);
    }
  }

  public Long getTimeToDrive(LatLng location, LatLng destination) {
    locationCorrector.correctLocation(destination);
    DistanceMatrix dm = geoApiClientFactory.createGeoApiClient().getDrivingDistance(destination, new LatLng[]{location});
    Long drivingTime = null;
    if (dm != null && dm.rows != null) {
      DistanceTime dt;
      try {
        dt = processDistanceMatrixElement(dm.rows[0].elements[0]);
        drivingTime = dt.getTime();
        if (drivingTime != null) {
          drivingTime = Math.max(drivingTime, expectationTime);
        }
      } catch (ServerError serverError) {
        log.warn(ERROR_MESSAGE, serverError);
      }
    }
    return drivingTime;
  }

  @Cacheable(cacheNames = {CacheConfiguration.ETC_CACHE}, key="#p0", cacheManager = "etcCacheManager")
  public Long getTimeToDriveCached(long rideId, LatLng location, LatLng destination) {
    return getTimeToDrive(location, destination);
  }

  public <T extends LocationAware & DistanceAware & DrivingTimeAware> void updateTimeToDrive(List<T> drivers, Double toLat,
    Double toLng, boolean addExpectationTime) {
    LatLng destination = new LatLng(toLat, toLng);
    locationCorrector.correctLocation(destination);

    if (drivers == null || drivers.isEmpty()) {
      return;
    }
    LatLng[] origins = drivers.stream()
      .filter(Objects::nonNull)
      .map(d -> new LatLng(d.getLatitude(), d.getLongitude()))
      .toArray(LatLng[]::new);

    DistanceMatrix dm = geoApiClientFactory.createGeoApiClient().getDrivingDistance(destination, origins);
    if (dm != null && dm.rows != null) {
      for (int i = 0; i < dm.rows.length; ++i) {
        Long drivingTime = null;
        Long drivingDistance = null;
        DistanceTime dt;
        try {
          dt = processDistanceMatrixElement(dm.rows[i].elements[0]);
          drivingTime = dt.getTime();
          drivingDistance = dt.getDistance();
        } catch (ServerError serverError) {
          log.warn(ERROR_MESSAGE, serverError);
        }

        // Check if the expectation time is to be added
        if (addExpectationTime && drivingTime != null) {
          drivingTime = Math.max(drivingTime, expectationTime);
        }
        drivers.get(i).setDrivingTimeToRider(drivingTime);
        drivers.get(i).setDrivingDistanceToRider(drivingDistance);
      }
    }
  }

  public Address normalizeAddress(Address address) throws RideAustinException {
    if (StringUtils.isNotBlank(address.getAddress()) && StringUtils.isNotBlank(address.getZipCode()) && !address.getAddress().contains(address.getZipCode())) {
      address.setAddress(address.getAddress().concat(" ").concat(address.getZipCode()));
    }
    GeocodingResult[] results = geoApiClientFactory.createGeoApiClient().geocodeAddress(address);
    if (results == null || results.length != 1) {
      throw new BadRequestException("Please enter the correct address");
    }
    GeocodingResult result = results[0];
    Address resultAddress = new Address();
    String zipCode = extractZipCode(result);
    resultAddress.setAddress(result.formattedAddress);
    resultAddress.setZipCode(zipCode);
    return resultAddress;
  }

  public Address reverseGeocodeAddress(double lat, double lng) throws RideAustinException {
    GeocodingResult[] results = geoApiClientFactory.createGeoApiClient().reverseGeocode(lat, lng);
    if (results != null && results.length > 0) {
        GeocodingResult result = results[0];
        return createAddress(result.addressComponents);
    }

    return null;
  }

  public Address retrieveAddress(String googlePlaceId) {
    final AddressComponent[] placeAddress = geoApiClientFactory.createGeoApiClient().retrieveAddress(googlePlaceId);
    return createAddress(placeAddress);
  }

  private Address createAddress(final AddressComponent[] addressComponents) {
    Address address = new Address();
    String streetNumber = "";
    String route = "";
    String locality = "";
    for (AddressComponent ac : addressComponents) {
      for (AddressComponentType act : ac.types) {
        switch (act) {
          case STREET_NUMBER:
            streetNumber = ac.shortName;
            break;
          case ROUTE:
            route = ac.shortName;
            break;
          case LOCALITY:
            locality = ac.shortName;
            break;
          case POSTAL_CODE:
            address.setZipCode(ac.shortName);
            break;
          default:
            break;
        }
      }
    }
    address.setAddress(String.format("%s %s, %s", streetNumber, route, locality));
    return address;
  }

  @Nonnull
  public byte[] generateMap(@Nonnull List<LatLng> points) throws ServerError {
    return getMap(points, GeoApiClientGoogleImpl.MAP_SCALE);
  }

  @Nonnull
  public byte[] generateMapMinimized(@Nonnull List<LatLng> points) throws ServerError {
    return getMap(points, 1);
  }

  private byte[] getMap(@Nonnull List<LatLng> points, int mapScale) throws ServerError {
    log.debug("Map generated for points.size: " + points.size());
    return restTemplate.execute(geoApiClientFactory.createGeoApiClient().getMapUrl(points, mapScale), HttpMethod.GET, null,
      response -> StreamUtils.copyToByteArray(response.getBody()));
  }

  private DistanceTime processDistanceMatrixElement(DistanceMatrixElement element) throws ServerError {
    long time;
    long distance;
    if (element != null) {
      switch (element.status) {
        case OK:
          distance = element.distance.inMeters;
          time = Optional.ofNullable(Optional.ofNullable(element.durationInTraffic)
            .orElse(element.duration)).map(d -> d.inSeconds).orElse(0L);
          break;
        case ZERO_RESULTS:
          time = 0L;
          distance = 0L;
          break;
        default:
          throw new ServerError(ERROR_MESSAGE);
      }
    } else {
      throw new ServerError(ERROR_MESSAGE);
    }
    return new DistanceTime(distance, time);
  }

  private String extractZipCode(GeocodingResult result) {
    return Stream.of(result.addressComponents)
      .filter(c -> ImmutableSet.copyOf(c.types).contains(AddressComponentType.POSTAL_CODE))
      .findAny()
      .map(c -> c.shortName)
      .orElse(null);
  }
}
