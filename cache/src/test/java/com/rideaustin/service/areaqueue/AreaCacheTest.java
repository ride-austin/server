package com.rideaustin.service.areaqueue;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableList;
import com.rideaustin.model.Area;
import com.rideaustin.repo.dsl.AreaDslRepository;

public class AreaCacheTest {

  @Mock
  private AreaDslRepository areaDslRepository;

  private AreaCache testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new AreaCache(areaDslRepository);
  }

  @Test
  public void getAreasPerCityFiltersAreasByCity() {
    final Area city1Area = new Area();
    final Area city2Area = new Area();
    city1Area.setCityId(1L);
    city2Area.setCityId(2L);
    when(areaDslRepository.findAll()).thenReturn(ImmutableList.of(
      city1Area, city2Area
    ));

    final List<Area> result = testedInstance.getAreasPerCity(1L);

    assertEquals(1, result.size());
    assertEquals(city1Area, result.get(0));
  }
}