package com.rideaustin.utils;

import java.math.BigDecimal;

public class SurgeFactorsMaxValidator extends BaseSurgeFactorValidator<SurgeFactorsMax> {

  @Override
  protected String getValue(SurgeFactorsMax constraintAnnotation) {
    return constraintAnnotation.value();
  }

  @Override
  protected boolean isInvalid(BigDecimal value) {
    return value.compareTo(threshold) > 0;
  }
}
