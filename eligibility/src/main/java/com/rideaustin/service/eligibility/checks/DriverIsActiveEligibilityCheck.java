package com.rideaustin.service.eligibility.checks;

import java.util.Collections;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.rideaustin.model.enums.DriverActivationStatus;
import com.rideaustin.model.user.Driver;
import com.rideaustin.service.eligibility.EligibilityCheck;
import com.rideaustin.service.eligibility.EligibilityCheckError;

@Component
@EligibilityCheck(targetClass = Driver.class)
public class DriverIsActiveEligibilityCheck extends BaseEligibilityCheckItem<Driver> {

  static final String MESSAGE = "Your account is not active - please contact support@example.com.";

  public DriverIsActiveEligibilityCheck() {
    super(Collections.emptyMap());
  }

  @Override
  public Optional<EligibilityCheckError> check(Driver subject) {
    if (subject != null &&
        (!subject.getUser().isEnabled()
          || !subject.isActive()
          || !DriverActivationStatus.ACTIVE.equals(subject.getActivationStatus())
        )
      ) {
      return Optional.of(new EligibilityCheckError(MESSAGE));
    }
    return Optional.empty();
  }
}
