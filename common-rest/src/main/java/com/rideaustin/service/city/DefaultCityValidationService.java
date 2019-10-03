package com.rideaustin.service.city;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.model.City;
import com.rideaustin.model.CityRestriction;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.ride.CityDriverType;
import com.rideaustin.repo.dsl.CityRestrictionDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.model.RideStartLocation;
import com.rideaustin.service.CityService;

public class DefaultCityValidationService implements CityValidationService {

  protected final CityService cityService;
  private final ObjectMapper objectMapper;
  private final CityRestrictionDslRepository restrictionRepository;

  public DefaultCityValidationService(CityService cityService, CityRestrictionDslRepository restrictionRepository, ObjectMapper objectMapper) {
    this.cityService = cityService;
    this.restrictionRepository = restrictionRepository;
    this.objectMapper = objectMapper;
  }

  public void validateCity(DocumentType documentType, Long cityId) throws BadRequestException {
    if (documentType.isCitySpecific()) {
      if (cityId == null) {
        throw new BadRequestException("Missing city id");
      }
      cityService.getCityOrThrow(cityId);
    }
  }

  public void validateCity(RideStartLocation startLocation, CityDriverType driverType, Long cityId) throws BadRequestException {
    City city = cityService.getCityOrThrow(cityId);

    final boolean cityValidationRequired = isCityValidationRequired(driverType);
    final boolean requestWithinCityArea = isRequestedWithinCityArea(startLocation, city);
    if (cityValidationRequired && !requestWithinCityArea) {
      throw new BadRequestException(String.format("Please select a pickup location within %s area.", city.getName()));
    }
    final List<CityRestriction> restrictedAreas = restrictionRepository.findByCity(cityId);
    for (CityRestriction area : restrictedAreas) {
      if (area.getAreaGeometry().getPolygon().contains(startLocation.getLat(), startLocation.getLng())) {
        throw new BadRequestException("Ride can't be requested now. Please try again later");
      }
    }
  }

  protected boolean isRequestedWithinCityArea(RideStartLocation startLocation, City city) {
    return city.getAreaGeometry().getPolygon()
        .contains(startLocation.getLat(), startLocation.getLng());
  }

  protected boolean isCityValidationRequired(CityDriverType driverType) {
    return driverType == null || driverType.getConfigurationObject(objectMapper).isCityValidationRequired();
  }
}