package com.rideaustin.service.eligibility.checks;

import static com.rideaustin.service.eligibility.checks.EligibilityCheckItem.Order.ORDER_DOES_NOT_MATTER;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import com.rideaustin.service.eligibility.EligibilityCheckError;

public abstract class BaseValidityDateEligibilityCheck<T> implements EligibilityCheckItem<T> {

  @Override
  public Optional<EligibilityCheckError> check(T subject) {
    Date validityDate = resolveValidityDate(subject);
    if (validityDate == null || validityDate.before(Date.from(Instant.now()))) {
      return raiseError();
    }
    return Optional.empty();
  }

  @Override
  public int getOrder() {
    return ORDER_DOES_NOT_MATTER;
  }

  protected abstract Date resolveValidityDate(T subject);

  protected abstract Optional<EligibilityCheckError> raiseError();
}
