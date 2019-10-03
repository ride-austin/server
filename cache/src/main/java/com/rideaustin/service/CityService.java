package com.rideaustin.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.rideaustin.Constants;
import com.rideaustin.filter.ClientAppVersionContext;
import com.rideaustin.model.City;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.model.Location;
import com.rideaustin.utils.map.MapUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CityService {

  private final CityCache cityCache;

  public List<City> findAll() {
    return cityCache.getAllCities();
  }

  public City getById(Long id) {
    return Optional.ofNullable(id)
      .map(cityCache::getCity)
      .orElseGet(this::getDefaultCity);
  }

  public City getByName(String name) throws NotFoundException {
    if (name == null) {
      return getDefaultCity();
    }

    return cityCache.getAllCities().stream().filter(c -> c.getName().equals(name)).findFirst().orElseThrow(() -> new NotFoundException("Invalid city"));
  }

  public List<Long> getCitiesIds() {
    return cityCache.getAllCities().stream().map(City::getId).collect(Collectors.toList());
  }

  public City findClosestByCoordinates(Location location) {
    return cityCache.getAllCities()
      .stream()
      .min(Comparator.comparing(city -> MapUtils.calculateDirectDistance(location.getLng(), location.getLat(),
        city.getAreaGeometry().getCenterPointLng(), city.getAreaGeometry().getCenterPointLat())))
      .orElseGet(this::getDefaultCity);
  }

  public City getDefaultCity() {
    return cityCache.getCity(Constants.DEFAULT_CITY_ID);
  }

  public City getCityOrThrow(Long cityId) throws BadRequestException {
    return Optional.ofNullable(cityId).map(cityCache::getCity).orElseThrow(() -> new BadRequestException("Invalid city id"));
  }

  public City getCityForCurrentClientAppVersionContext() {

    if (ClientAppVersionContext.getAppVersion() == null) {
      return getById(Constants.DEFAULT_CITY_ID);
    }
    if (ClientAppVersionContext.getAppVersion().getUserAgent() == null) {
      return getById(Constants.DEFAULT_CITY_ID);
    }
    if (ClientAppVersionContext.getAppVersion().getUserAgent().toLowerCase().contains("austin")) {
      return getById(1L);
    }
    if (ClientAppVersionContext.getAppVersion().getUserAgent().toLowerCase().contains("houston")) {
      return getById(2L);
    }
    return getById(Constants.DEFAULT_CITY_ID);
  }
}

