package com.rideaustin.service.batch;

import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.rideaustin.model.enums.DriverActivationStatus;
import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.dsl.DriverDslRepository;
import com.rideaustin.service.model.DriverBatchUpdateError;

@Component
public class ActivationBatchValidator implements BatchRecordValidator {

  private final DriverDslRepository driverDslRepository;

  @Inject
  public ActivationBatchValidator(DriverDslRepository driverDslRepository) {
    this.driverDslRepository = driverDslRepository;
  }

  @Override
  public Optional<DriverBatchUpdateError> validate(int rowNumber, DriverBatchUpdateDto record) {
    Driver driver = driverDslRepository.findById(record.getId());
    if (driver != null && (record.getActive() != null || record.getActivationStatus() != null)) {
      boolean active = Optional.ofNullable(record.getActive()).orElse(driver.isActive());
      boolean activated = DriverActivationStatus.ACTIVE.equals(Optional.ofNullable(record.getActivationStatus()).orElse(driver.getActivationStatus()));

      if (active ^ activated) {
        return Optional.of(new DriverBatchUpdateError(rowNumber, "active", String.valueOf(active), "Incorrect combination of active and activationStatus field values"));
      }
    }
    return Optional.empty();
  }
}
