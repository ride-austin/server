package com.rideaustin.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;

import javax.validation.Payload;

import org.junit.Before;
import org.junit.Test;

public class SurgeFactorsMaxValidatorTest {

  private static final int THRESHOLD = 5;
  private SurgeFactorsMaxValidator testedInstance;

  @Before
  public void setUp() throws Exception {
    testedInstance = new SurgeFactorsMaxValidator();
    testedInstance.initialize(new SurgeFactorsMax(){

      @Override
      public Class<? extends Annotation> annotationType() {
        return null;
      }

      @Override
      public String message() {
        return null;
      }

      @Override
      public Class<?>[] groups() {
        return new Class[0];
      }

      @Override
      public Class<? extends Payload>[] payload() {
        return new Class[0];
      }

      @Override
      public String value() {
        return String.valueOf(THRESHOLD);
      }
    });
  }

  @Test
  public void isValidReturnsTrueWhenValueGreaterThanThreshold() {
    final boolean result = testedInstance.isInvalid(BigDecimal.valueOf(THRESHOLD + 1));

    assertTrue(result);
  }

  @Test
  public void isValidReturnsFalseWhenValueEqualToThreshold() {
    final boolean result = testedInstance.isInvalid(BigDecimal.valueOf(THRESHOLD));

    assertFalse(result);
  }

  @Test
  public void isValidReturnsFalseWhenValueLessThanThreshold() {
    final boolean result = testedInstance.isInvalid(BigDecimal.valueOf(THRESHOLD - 1));

    assertFalse(result);
  }

}