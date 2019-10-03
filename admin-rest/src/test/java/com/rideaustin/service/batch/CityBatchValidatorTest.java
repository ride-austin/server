package com.rideaustin.service.batch;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.model.City;
import com.rideaustin.service.CityCache;
import com.rideaustin.service.model.DriverBatchUpdateError;

public class CityBatchValidatorTest {

  @Mock
  private CityCache cityCache;

  private CityBatchValidator testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    testedInstance = new CityBatchValidator(cityCache);

    City city = new City();
    city.setId(1L);
    city.setName("Austin");
    when(cityCache.getAllCities()).thenReturn(Collections.singletonList(city));
  }

  @Test
  public void testValidateReturnsNoErrorIfValidIdIsProvided() {
    Optional<DriverBatchUpdateError> result = testedInstance.validate("1", null, 0);

    assertFalse(result.isPresent());
  }

  @Test
  public void testValidateReturnsNoErrorIfValidNameIsProvided() {
    Optional<DriverBatchUpdateError> result = testedInstance.validate("austin", null, 0);

    assertFalse(result.isPresent());
  }

  @Test
  public void testValidateReturnsErrorIfNoValidIdOrNameIsProvided() {
    Optional<DriverBatchUpdateError> result = testedInstance.validate("chicago", null, 0);

    assertTrue(result.isPresent());
  }

}