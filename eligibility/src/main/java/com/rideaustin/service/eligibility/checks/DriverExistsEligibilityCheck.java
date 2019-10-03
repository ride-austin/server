package com.rideaustin.service.eligibility.checks;

import java.util.Collections;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.rideaustin.model.user.Driver;
import com.rideaustin.service.eligibility.EligibilityCheck;
import com.rideaustin.service.eligibility.EligibilityCheckError;

@Component
@EligibilityCheck(targetClass = Driver.class)
public class DriverExistsEligibilityCheck extends BaseEligibilityCheckItem<Driver> {

  static final String MESSAGE = "Driver does not exist";

  public DriverExistsEligibilityCheck() {
    super(Collections.emptyMap());
  }

  @Override
  public Optional<EligibilityCheckError> check(Driver subject) {
    if (subject == null) {
      return Optional.of(new EligibilityCheckError(MESSAGE));
    }
    return Optional.empty();
  }
}
