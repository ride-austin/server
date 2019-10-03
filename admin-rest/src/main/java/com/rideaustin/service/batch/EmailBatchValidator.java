package com.rideaustin.service.batch;

import java.lang.reflect.Field;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Component;

import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.dsl.DriverDslRepository;
import com.rideaustin.service.model.DriverBatchUpdateError;

@Component
public class EmailBatchValidator implements BatchRecordValidator, BatchFieldValidator {

  private final DriverDslRepository driverDslRepository;
  private final EmailValidator emailValidator;

  @Inject
  public EmailBatchValidator(DriverDslRepository driverDslRepository) {
    this.driverDslRepository = driverDslRepository;
    this.emailValidator = EmailValidator.getInstance();
  }

  @Override
  public Optional<DriverBatchUpdateError> validate(int rowNumber, DriverBatchUpdateDto record) {
    Long existingDriverWithEmail = Optional.ofNullable(driverDslRepository.findByEmail(record.getEmail()))
      .map(Driver::getId).orElse(null);
    if (existingDriverWithEmail != null && !existingDriverWithEmail.equals(record.getId())) {
      return Optional.of(new DriverBatchUpdateError(rowNumber, "Email", record.getEmail(), "Provided email is already used by another driver"));
    }
    return Optional.empty();
  }

  @Override
  public Optional<DriverBatchUpdateError> validate(String value, Field field, int rowNumber) {
    if (!emailValidator.isValid(value)) {
      return Optional.of(new DriverBatchUpdateError(rowNumber, "Email", value, "Email address is incorrect"));
    }
    return Optional.empty();
  }
}
