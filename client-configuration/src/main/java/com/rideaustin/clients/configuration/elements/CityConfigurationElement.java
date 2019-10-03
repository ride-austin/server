package com.rideaustin.clients.configuration.elements;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;
import com.google.maps.model.LatLng;
import com.rideaustin.clients.configuration.ConfigurationElement;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.City;
import com.rideaustin.rest.model.Location;
import com.rideaustin.service.CityService;
import com.rideaustin.utils.GeometryUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CityConfigurationElement implements ConfigurationElement {

  private final CityService cityService;

  @Override
  public Map getConfiguration(ClientType clientType, Location location, Long cityId) {
    return ImmutableMap.of("supportedCities", getSupportedCities(),
      "currentCity", getFullCityInfo(cityService.getById(cityId)));
  }

  @Override
  public Map getDefaultConfiguration(ClientType clientType, Location location, Long cityId) {
    return getConfiguration(clientType, location, cityId);
  }

  private List<ShortCityInfo> getSupportedCities() {
    return cityService.findAll().stream().filter(City::isEnabled).map(this::getShortCityInfo).collect(Collectors.toList());
  }

  private ShortCityInfo getShortCityInfo(City city) {
    return new ShortCityInfo(city.getId(), city.getName(), city.getLogoUrl(), city.getLogoUrlDark());
  }

  private FullCityInfo getFullCityInfo(City city) {
    return new FullCityInfo(city.getId(), city.getName(),
      String.format("%s %s", city.getAreaGeometry().getCenterPointLat(), city.getAreaGeometry().getCenterPointLng()),
      new LatLng(city.getAreaGeometry().getCenterPointLat(), city.getAreaGeometry().getCenterPointLng()),
      GeometryUtils.buildCoordinates(city.getAreaGeometry().getCsvGeometry())
    );
  }

  @Getter
  @RequiredArgsConstructor
  static class ShortCityInfo {
    private final long cityId;
    private final String cityName;
    private final String logoUrl;
    private final String logoBlackUrl;
  }

  @Getter
  @RequiredArgsConstructor
  static class FullCityInfo {
    private final long cityId;
    private final String cityName;
    private final String cityCenterLocation;
    private final LatLng cityCenterLocationData;
    private final List<LatLng> cityBoundaryPolygon;
  }

}