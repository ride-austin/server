package com.rideaustin.utils;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.service.user.CarTypesCache;
import com.rideaustin.service.user.CarTypesUtils;

public class SurgeFactorsValidatorTest {

  @Mock
  private CarTypesCache carTypesCache;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    CarTypesUtils.setCarTypesCache(carTypesCache);
  }

  @Test
  public void isValidReturnsFalseOnInvalidCarCategory() {
    final String category = "CAR";
    final SurgeFactorsValidator testedInstance = new SurgeFactorsValidator();
    when(carTypesCache.getCarType(category)).thenReturn(null);

    final boolean result = testedInstance.isValid(ImmutableMap.of(
      category, BigDecimal.ONE
    ), null);

    assertFalse(result);
  }

  @Test
  public void isValidReturnsTrueOnValidCarCategory() {
    final String category = "REGULAR";
    final SurgeFactorsValidator testedInstance = new SurgeFactorsValidator();
    when(carTypesCache.getCarType(category)).thenReturn(new CarType());

    final boolean result = testedInstance.isValid(ImmutableMap.of(
      category, BigDecimal.ONE
    ), null);

    assertTrue(result);
  }
}