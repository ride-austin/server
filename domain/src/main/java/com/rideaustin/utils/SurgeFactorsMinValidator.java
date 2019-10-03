package com.rideaustin.utils;

import java.math.BigDecimal;

public final class SurgeFactorsMinValidator extends BaseSurgeFactorValidator<SurgeFactorsMin> {

  @Override
  protected String getValue(SurgeFactorsMin constraintAnnotation) {
    return constraintAnnotation.value();
  }

  @Override
  protected boolean isInvalid(BigDecimal value) {
    return value.compareTo(threshold) < 0;
  }
}
