package com.rideaustin.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableSet;
import com.rideaustin.model.City;
import com.rideaustin.model.surgepricing.AreaGeometry;
import com.rideaustin.repo.dsl.CityDslRepository;

public class CityCacheTest {

  @Mock
  private CityDslRepository cityDslRepository;

  private CityCache testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new CityCache(cityDslRepository);
  }

  @Test
  public void toBitMaskSumsCityBitmasks() {
    final City city1 = new City();
    final City city2 = new City();
    setupCities(city1, city2);
    testedInstance.refreshCache();

    final int result = testedInstance.toBitMask(ImmutableSet.of(3L, 4L));

    assertEquals(city1.getBitmask() | city2.getBitmask(), result);
  }

  @Test
  public void fromBitMaskCollectsCityIds() {
    final City city1 = new City();
    final City city2 = new City();
    setupCities(city1, city2);
    testedInstance.refreshCache();

    final Set<Long> result = testedInstance.fromBitMask(city1.getBitmask() | city2.getBitmask());

    assertTrue(CollectionUtils.isEqualCollection(ImmutableSet.of(3L, 4L), result));
  }

  private void setupCities(City city1, City city2) {
    city1.setId(3L);
    city1.setBitmask(1);
    final AreaGeometry areaGeometry1 = new AreaGeometry();
    areaGeometry1.setCsvGeometry("-97.48616,34.68419 -97.68461,34.98191 -97.61681,34.94198");
    city1.setAreaGeometry(areaGeometry1);
    city2.setId(4L);
    city2.setBitmask(2);
    final AreaGeometry areaGeometry2 = new AreaGeometry();
    areaGeometry2.setCsvGeometry("-97.48616,34.68419 -97.68461,34.98191 -97.61681,34.94198");
    city2.setAreaGeometry(areaGeometry2);
    when(cityDslRepository.findAllEnabled()).thenReturn(Arrays.asList(city1, city2));
  }
}