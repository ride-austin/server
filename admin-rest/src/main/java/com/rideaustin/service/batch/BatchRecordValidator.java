package com.rideaustin.service.batch;

import java.util.Optional;

import com.rideaustin.service.model.DriverBatchUpdateError;

public interface BatchRecordValidator extends BatchValidator {
  Optional<DriverBatchUpdateError> validate(int rowNumber, DriverBatchUpdateDto record);
}
