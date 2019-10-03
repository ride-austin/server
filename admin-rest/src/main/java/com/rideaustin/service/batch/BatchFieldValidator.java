package com.rideaustin.service.batch;

import java.lang.reflect.Field;
import java.util.Optional;

import com.rideaustin.service.model.DriverBatchUpdateError;

public interface BatchFieldValidator extends BatchValidator {
  Optional<DriverBatchUpdateError> validate(String value, Field field, int rowNumber);
}
