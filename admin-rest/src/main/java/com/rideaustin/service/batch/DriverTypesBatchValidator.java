package com.rideaustin.service.batch;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.rideaustin.service.model.DriverBatchUpdateError;
import com.rideaustin.service.user.DriverTypeCache;

@Component
public class DriverTypesBatchValidator implements BatchFieldValidator {

  private final DriverTypeCache driverTypeCache;

  @Inject
  public DriverTypesBatchValidator(DriverTypeCache driverTypeCache) {
    this.driverTypeCache = driverTypeCache;
  }

  @Override
  public Optional<DriverBatchUpdateError> validate(String value, Field field, int rowNumber) {
    Set<String> types = Arrays.stream(value.split(","))
      .map(String::trim)
      .filter(BatchValidator.notEmpty())
      .collect(Collectors.toSet());
    for (String type : types) {
      if (driverTypeCache.getDriverType(type) == null) {
        return Optional.of(new DriverBatchUpdateError(rowNumber, "Driver types", type, "Unknown driver type"));
      }
    }
    return Optional.empty();
  }
}
