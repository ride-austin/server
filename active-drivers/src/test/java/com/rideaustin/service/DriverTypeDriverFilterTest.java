package com.rideaustin.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.ride.CityDriverType;
import com.rideaustin.model.ride.CityDriverType.DefaultDriverTypeConfiguration;
import com.rideaustin.service.DefaultSearchDriverHandler.DriverTypeDriverFilter;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.service.user.DriverTypeCache;

public class DriverTypeDriverFilterTest {

  @Mock
  private DriverTypeCache driverTypeCache;

  private DriverTypeDriverFilter testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testFilterWhenDriverTypeIsNotRequestedAndDriverHasNoTypesAssigned() {
    final ActiveDriverSearchCriteria searchCriteria = new ActiveDriverSearchCriteria(0.0, 0.0,
      Collections.emptyList(), 1, "", 1, 1L, null, 10, 10);
    testedInstance = new DriverTypeDriverFilter(driverTypeCache, searchCriteria, Collections.emptyMap());

    final OnlineDriverDto activeDriver = new OnlineDriverDto();
    activeDriver.setAvailableDriverTypesBitmask(null);
    final boolean result = testedInstance.filter(activeDriver);

    assertTrue(result);
  }

  @Test
  public void testFilterWhenDriverTypeIsExclusiveAndDriverHasNoTypesAssigned() {
    final ActiveDriverSearchCriteria searchCriteria = new ActiveDriverSearchCriteria(0.0, 0.0,
      Collections.emptyList(), 1, "", 1, 1L, 1, 10, 10);
    testedInstance = new DriverTypeDriverFilter(driverTypeCache, searchCriteria, Collections.emptyMap());
    final CityDriverType cityDriverType = mock(CityDriverType.class);
    final DefaultDriverTypeConfiguration configuration = new DefaultDriverTypeConfiguration();
    configuration.setExclusive(true);
    Integer driverTypeBitmask = 1;
    when(cityDriverType.getBitmask()).thenReturn(driverTypeBitmask);
    when(cityDriverType.getConfigurationObject(any(ObjectMapper.class))).thenReturn(configuration);
    when(driverTypeCache.getByCityAndBitmask(1L, driverTypeBitmask)).thenReturn(Collections.singleton(cityDriverType));

    final OnlineDriverDto activeDriver = new OnlineDriverDto();
    activeDriver.setAvailableDriverTypesBitmask(null);
    final boolean result = testedInstance.filter(activeDriver);

    assertFalse(result);
  }

  @Test
  public void testFilterWhenDriverTypeIsNonExclusiveAndDriverHasNoTypesAssigned() {
    final ActiveDriverSearchCriteria searchCriteria = new ActiveDriverSearchCriteria(0.0, 0.0,
      Collections.emptyList(), 1, "", 1, 1L, 1, 10, 10);
    testedInstance = new DriverTypeDriverFilter(driverTypeCache, searchCriteria, Collections.emptyMap());
    final CityDriverType cityDriverType = mock(CityDriverType.class);
    final DefaultDriverTypeConfiguration configuration = new DefaultDriverTypeConfiguration();
    configuration.setExclusive(false);
    final int driverTypeBitmask = 1;
    when(cityDriverType.getBitmask()).thenReturn(driverTypeBitmask);
    when(cityDriverType.getConfigurationObject(any(ObjectMapper.class))).thenReturn(configuration);
    when(driverTypeCache.getByCityAndBitmask(1L, driverTypeBitmask)).thenReturn(Collections.singleton(cityDriverType));

    final OnlineDriverDto activeDriver = new OnlineDriverDto();
    activeDriver.setAvailableDriverTypesBitmask(null);
    final boolean result = testedInstance.filter(activeDriver);

    assertFalse(result);
  }

  @Test
  public void testFilterWhenDriverTypeIsExclusiveAndDriverHasSameExclusiveType() {
    final int driverTypeBitmask = 1;
    final long driverId = ThreadLocalRandom.current().nextLong();
    final ActiveDriverSearchCriteria searchCriteria = new ActiveDriverSearchCriteria(0.0, 0.0,
      Collections.emptyList(), 1, "", 1, 1L, driverTypeBitmask, 10, 10);
    testedInstance = new DriverTypeDriverFilter(driverTypeCache, searchCriteria, ImmutableMap.of(driverId, driverTypeBitmask));
    final CityDriverType cityDriverType = mock(CityDriverType.class);
    final DefaultDriverTypeConfiguration configuration = new DefaultDriverTypeConfiguration();
    configuration.setExclusive(true);
    when(cityDriverType.getConfigurationObject(any(ObjectMapper.class))).thenReturn(configuration);
    when(driverTypeCache.getByCityAndBitmask(1L, driverTypeBitmask)).thenReturn(Collections.singleton(cityDriverType));

    final OnlineDriverDto activeDriver = new OnlineDriverDto(driverId, ActiveDriverStatus.AVAILABLE, 0L, 0L, "", "", "");
    activeDriver.setAvailableDriverTypesBitmask(driverTypeBitmask);
    final boolean result = testedInstance.filter(activeDriver);

    assertTrue(result);
  }

  @Test
  public void testFilterWhenDriverTypeIsExclusiveAndDriverHasAnotherExclusiveType() {
    final int driverTypeBitmask = 1;
    final int requestedBitmask = 2;
    final long driverId = ThreadLocalRandom.current().nextLong();
    final ActiveDriverSearchCriteria searchCriteria = new ActiveDriverSearchCriteria(0.0, 0.0,
      Collections.emptyList(), 1, "", 1, 1L, requestedBitmask, 10, 10);
    testedInstance = new DriverTypeDriverFilter(driverTypeCache, searchCriteria, Collections.emptyMap());
    final CityDriverType cityDriverType = mock(CityDriverType.class);
    final DefaultDriverTypeConfiguration configuration = new DefaultDriverTypeConfiguration();
    configuration.setExclusive(true);
    when(cityDriverType.getBitmask()).thenReturn(driverTypeBitmask);
    when(cityDriverType.getConfigurationObject(any(ObjectMapper.class))).thenReturn(configuration);
    when(driverTypeCache.getByCityAndBitmask(1L, driverTypeBitmask)).thenReturn(Collections.singleton(cityDriverType));

    final OnlineDriverDto activeDriver = new OnlineDriverDto(driverId, ActiveDriverStatus.AVAILABLE, 0L, 0L, "", "", "");
    activeDriver.setAvailableDriverTypesBitmask(driverTypeBitmask);
    final boolean result = testedInstance.filter(activeDriver);

    assertFalse(result);
  }

  @Test
  public void testFilterWhenDriverTypeIsNotRequestedAndDriverHasExclusiveType() {
    final int driverTypeBitmask = 1;
    final long driverId = ThreadLocalRandom.current().nextLong();
    final ActiveDriverSearchCriteria searchCriteria = new ActiveDriverSearchCriteria(0.0, 0.0,
      Collections.emptyList(), 1, "", 1, 1L, null, 10, 10);
    testedInstance = new DriverTypeDriverFilter(driverTypeCache, searchCriteria, ImmutableMap.of(driverId, driverTypeBitmask));
    final CityDriverType cityDriverType = mock(CityDriverType.class);
    final DefaultDriverTypeConfiguration configuration = new DefaultDriverTypeConfiguration();
    configuration.setExclusive(true);
    when(cityDriverType.getBitmask()).thenReturn(driverTypeBitmask);
    when(cityDriverType.getConfigurationObject(any(ObjectMapper.class))).thenReturn(configuration);
    when(driverTypeCache.getByCityAndBitmask(1L, driverTypeBitmask)).thenReturn(Collections.singleton(cityDriverType));

    final OnlineDriverDto activeDriver = new OnlineDriverDto(driverId, ActiveDriverStatus.AVAILABLE, 0L, 0L, "", "", "");
    activeDriver.setAvailableDriverTypesBitmask(driverTypeBitmask);
    final boolean result = testedInstance.filter(activeDriver);

    assertFalse(result);
  }

  @Test
  public void testFilterWhenDriverTypeIsNotRequestedAndDriverHasNonExclusiveType() {
    final int driverTypeBitmask = 1;
    final long driverId = ThreadLocalRandom.current().nextLong();
    final ActiveDriverSearchCriteria searchCriteria = new ActiveDriverSearchCriteria(0.0, 0.0,
      Collections.emptyList(), 1, "", 1, 1L, null, 10, 10);
    testedInstance = new DriverTypeDriverFilter(driverTypeCache, searchCriteria, ImmutableMap.of(driverId, driverTypeBitmask));
    final CityDriverType cityDriverType = mock(CityDriverType.class);
    final DefaultDriverTypeConfiguration configuration = new DefaultDriverTypeConfiguration();
    configuration.setExclusive(false);
    when(cityDriverType.getBitmask()).thenReturn(driverTypeBitmask);
    when(cityDriverType.getConfigurationObject(any(ObjectMapper.class))).thenReturn(configuration);
    when(driverTypeCache.getByCityAndBitmask(1L, driverTypeBitmask)).thenReturn(Collections.singleton(cityDriverType));

    final OnlineDriverDto activeDriver = new OnlineDriverDto(driverId, ActiveDriverStatus.AVAILABLE, 0L, 0L, "", "", "");
    activeDriver.setAvailableDriverTypesBitmask(driverTypeBitmask);
    final boolean result = testedInstance.filter(activeDriver);

    assertTrue(result);
  }
}
