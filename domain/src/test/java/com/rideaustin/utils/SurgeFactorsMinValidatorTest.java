package com.rideaustin.utils;

import static org.junit.Assert.*;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;

import javax.validation.Payload;

import org.junit.Before;
import org.junit.Test;

public class SurgeFactorsMinValidatorTest {

  private static final int THRESHOLD = 2;
  private SurgeFactorsMinValidator testedInstance;

  @Before
  public void setUp() throws Exception {
    testedInstance = new SurgeFactorsMinValidator();
    testedInstance.initialize(new SurgeFactorsMin(){

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
  public void isValidReturnsFalseWhenValueGreaterThanThreshold() {
    final boolean result = testedInstance.isInvalid(BigDecimal.valueOf(THRESHOLD + 1));

    assertFalse(result);
  }

  @Test
  public void isValidReturnsFalseWhenValueEqualToThreshold() {
    final boolean result = testedInstance.isInvalid(BigDecimal.valueOf(THRESHOLD));

    assertFalse(result);
  }

  @Test
  public void isValidReturnsTrueWhenValueLessThanThreshold() {
    final boolean result = testedInstance.isInvalid(BigDecimal.valueOf(THRESHOLD - 1));

    assertTrue(result);
  }

}