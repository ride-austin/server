package com.rideaustin.service.batch;

import java.lang.reflect.Field;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.rideaustin.model.City;
import com.rideaustin.service.CityCache;
import com.rideaustin.service.model.DriverBatchUpdateError;

@Component
public class CityBatchValidator implements BatchFieldValidator {

  private final CityCache cityCache;

  @Inject
  public CityBatchValidator(CityCache cityCache) {
    this.cityCache = cityCache;
  }

  @Override
  public Optional<DriverBatchUpdateError> validate(String value, Field field, int rowNumber) {
    boolean valid = false;
    for (City city : cityCache.getAllCities()) {
      if (city.getName().equalsIgnoreCase(value) || String.valueOf(city.getId()).equals(value)) {
        valid = true;
        break;
      }
    }
    return valid ? Optional.empty() : Optional.of(new DriverBatchUpdateError(rowNumber, "city", value,
      "Invalid city. Please provide valid city id or name"));
  }
}
