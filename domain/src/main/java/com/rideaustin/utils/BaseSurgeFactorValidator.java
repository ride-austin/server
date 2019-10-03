package com.rideaustin.utils;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.Map;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public abstract class BaseSurgeFactorValidator<T extends Annotation> implements ConstraintValidator<T, Map<String, BigDecimal>> {

  protected BigDecimal threshold;

  @Override
  public void initialize(T constraintAnnotation) {
    threshold = BigDecimal.valueOf(Double.valueOf(getValue(constraintAnnotation)));
  }

  @Override
  public boolean isValid(Map<String, BigDecimal> map, ConstraintValidatorContext context) {
    for (BigDecimal value : map.values()) {
      if (isInvalid(value)) {
        return false;
      }
    }
    return true;
  }

  protected abstract String getValue(T constraintAnnotation);

  protected abstract boolean isInvalid(BigDecimal value);
}
