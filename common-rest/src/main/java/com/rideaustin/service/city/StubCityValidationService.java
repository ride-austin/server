package com.rideaustin.service.city;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.model.City;
import com.rideaustin.model.ride.CityDriverType;
import com.rideaustin.repo.dsl.CityRestrictionDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.model.RideStartLocation;
import com.rideaustin.service.CityService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StubCityValidationService extends DefaultCityValidationService {

  public StubCityValidationService(CityService cityService, CityRestrictionDslRepository restrictionRepository, ObjectMapper mapper) {
    super(cityService, restrictionRepository, mapper);
  }

  @Override
  public void validateCity(RideStartLocation startLocation, CityDriverType driverType, Long cityId) throws BadRequestException {
    City city = cityService.getCityOrThrow(cityId);
    log.info(String.format("[CITYVALIDATION] Required: %s, Within: %s", isCityValidationRequired(driverType),
      isRequestedWithinCityArea(startLocation, city)));
  }
}
