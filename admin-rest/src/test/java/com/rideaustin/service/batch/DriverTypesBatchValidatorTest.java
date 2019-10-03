package com.rideaustin.service.batch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.model.ride.DriverType;
import com.rideaustin.service.model.DriverBatchUpdateError;
import com.rideaustin.service.user.DriverTypeCache;

public class DriverTypesBatchValidatorTest {

  @Mock
  private DriverTypeCache driverTypeCache;

  private DriverTypesBatchValidator testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new DriverTypesBatchValidator(driverTypeCache);
  }

  @Test
  public void validateReturnsNoErrorWhenAllTypesArePresentInCache() {
    final String driverType = "DIRECT_CONNECT";
    when(driverTypeCache.getDriverType(eq(driverType))).thenReturn(new DriverType());

    final Optional<DriverBatchUpdateError> result = testedInstance.validate(driverType, null, 0);

    assertFalse(result.isPresent());
  }

  @Test
  public void validateReturnsNoErrorWhenValueIsEmpty() {
    final Optional<DriverBatchUpdateError> result = testedInstance.validate("", null, 0);

    assertFalse(result.isPresent());
  }

  @Test
  public void validateReturnsErrorWhenDriverTypeNotFound() {
    final String driverType = "DIRECT_CONNECT";

    final Optional<DriverBatchUpdateError> result = testedInstance.validate(driverType, null, 0);

    assertTrue(result.isPresent());
    assertEquals(1, result.get().getRowNumber());
    assertEquals("Driver types", result.get().getField());
    assertEquals(driverType, result.get().getValue());
    assertEquals("Unknown driver type", result.get().getMessage());
  }
}