package com.rideaustin.utils;

import java.math.BigDecimal;
import java.util.Map;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.rideaustin.service.user.CarTypesUtils;

public class SurgeFactorsValidator implements ConstraintValidator<SurgeFactors, Map<String, BigDecimal>> {
  public void initialize(SurgeFactors constraint) {
    //do nothing
  }

  public boolean isValid(Map<String, BigDecimal> obj, ConstraintValidatorContext context) {
    for (Map.Entry<String, BigDecimal> entry : obj.entrySet()) {
      if (isCarCategoryInvalid(entry)) {
        return false;
      }
    }
    return true;
  }

  private boolean isCarCategoryInvalid(Map.Entry<String, BigDecimal> entry) {
    return CarTypesUtils.getCarType(entry.getKey()) == null;
  }
}
