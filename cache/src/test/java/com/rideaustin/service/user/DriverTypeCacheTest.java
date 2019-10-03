package com.rideaustin.service.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.rideaustin.model.ride.CityDriverType;
import com.rideaustin.model.ride.DriverType;
import com.rideaustin.repo.dsl.DriverTypeDslRepository;

public class DriverTypeCacheTest {

  @Mock
  private DriverTypeDslRepository driverTypeDslRepository;

  private DriverTypeCache testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new DriverTypeCache(driverTypeDslRepository);
  }

  @Test
  public void toBitmaskReturnsNullWhenCollectionIsNull() {
    final DriverType womenOnly = setupDriverType(1, "WOMEN_ONLY");
    final DriverType directConnect = setupDriverType(2, "DIRECT_CONNECT");
    when(driverTypeDslRepository.getAllEnabledOrdered()).thenReturn(ImmutableList.of(womenOnly, directConnect));
    testedInstance.refreshCache();

    final Integer result = testedInstance.toBitMask(null);

    assertNull(result);
  }

  @Test
  public void toBitmaskReturnsNullWhenCollectionIsEmpty() {
    final DriverType womenOnly = setupDriverType(1, "WOMEN_ONLY");
    final DriverType directConnect = setupDriverType(2, "DIRECT_CONNECT");
    when(driverTypeDslRepository.getAllEnabledOrdered()).thenReturn(ImmutableList.of(womenOnly, directConnect));
    testedInstance.refreshCache();

    final Integer result = testedInstance.toBitMask(Collections.emptySet());

    assertNull(result);
  }

  @Test
  public void toBitmaskReturnsNullWhenCollectionContainsNulls() {
    final DriverType womenOnly = setupDriverType(1, "WOMEN_ONLY");
    final DriverType directConnect = setupDriverType(2, "DIRECT_CONNECT");
    when(driverTypeDslRepository.getAllEnabledOrdered()).thenReturn(ImmutableList.of(womenOnly, directConnect));
    testedInstance.refreshCache();

    final HashSet<String> nullCollection = new HashSet<>();
    nullCollection.add(null);
    final Integer result = testedInstance.toBitMask(nullCollection);

    assertNull(result);
  }

  @Test
  public void toBitmaskReturnsNullWhenCollectionContainsEmptyString() {
    final DriverType womenOnly = setupDriverType(1, "WOMEN_ONLY");
    final DriverType directConnect = setupDriverType(2, "DIRECT_CONNECT");
    when(driverTypeDslRepository.getAllEnabledOrdered()).thenReturn(ImmutableList.of(womenOnly, directConnect));
    testedInstance.refreshCache();

    final Integer result = testedInstance.toBitMask(ImmutableSet.of(""));

    assertNull(result);
  }

  @Test
  public void toBitmaskSumsDriverTypeBitmasks() {
    final DriverType womenOnly = setupDriverType(1, "WOMEN_ONLY");
    final DriverType directConnect = setupDriverType(2, "DIRECT_CONNECT");
    when(driverTypeDslRepository.getAllEnabledOrdered()).thenReturn(ImmutableList.of(womenOnly, directConnect));
    testedInstance.refreshCache();

    final int result = testedInstance.toBitMask(ImmutableSet.of("WOMEN_ONLY", "DIRECT_CONNECT"));

    assertEquals(3, result);
  }

  @Test
  public void fromBitmaskCollectsCarCategories() {
    final DriverType womenOnly = setupDriverType(1, "WOMEN_ONLY");
    final DriverType directConnect = setupDriverType(2, "DIRECT_CONNECT");
    when(driverTypeDslRepository.getAllEnabledOrdered()).thenReturn(ImmutableList.of(womenOnly, directConnect));
    testedInstance.refreshCache();

    final Set<String> result = testedInstance.fromBitMask(womenOnly.getBitmask() | directConnect.getBitmask());

    assertTrue(CollectionUtils.isEqualCollection(result, ImmutableSet.of("WOMEN_ONLY", "DIRECT_CONNECT")));
  }

  @Test
  public void getByCityAndBitmaskReturnsEmptySetWhenBitmaskIsNull() {
    final DriverType womenOnly = setupDriverType(1, "WOMEN_ONLY");
    final DriverType directConnect = setupDriverType(2, "DIRECT_CONNECT");
    when(driverTypeDslRepository.getAllEnabledOrdered()).thenReturn(ImmutableList.of(womenOnly, directConnect));
    testedInstance.refreshCache();

    final Set<CityDriverType> result = testedInstance.getByCityAndBitmask(1L, null);

    assertTrue(result.isEmpty());
  }

  @Test
  public void getByCityAndBitmaskReturnsEmptySetWhenNoCityIsPresent() {
    final DriverType womenOnly = setupDriverType(1, "WOMEN_ONLY");
    final DriverType directConnect = setupDriverType(2, "DIRECT_CONNECT");
    when(driverTypeDslRepository.getAllEnabledOrdered()).thenReturn(ImmutableList.of(womenOnly, directConnect));
    testedInstance.refreshCache();

    final Set<CityDriverType> result = testedInstance.getByCityAndBitmask(2L, 1);

    assertTrue(result.isEmpty());
  }

  @Test
  public void getByCityAndBitmaskReturnsCityDriverType() {
    final DriverType womenOnly = setupDriverType(1, "WOMEN_ONLY");
    final DriverType directConnect = setupDriverType(2, "DIRECT_CONNECT");
    when(driverTypeDslRepository.getAllEnabledOrdered()).thenReturn(ImmutableList.of(womenOnly, directConnect));
    testedInstance.refreshCache();

    final Set<CityDriverType> result = testedInstance.getByCityAndBitmask(1L, 1);

    assertEquals(1, result.size());
    assertEquals(womenOnly, result.iterator().next().getDriverType());
  }

  @Test
  public void getDriverTypeReturnsNullWhenNameIsNull() {
    final DriverType womenOnly = setupDriverType(1, "WOMEN_ONLY");
    final DriverType directConnect = setupDriverType(2, "DIRECT_CONNECT");
    when(driverTypeDslRepository.getAllEnabledOrdered()).thenReturn(ImmutableList.of(womenOnly, directConnect));
    testedInstance.refreshCache();

    final DriverType result = testedInstance.getDriverType(null);

    assertNull(result);
  }

  @Test
  public void getDriverTypeReturnsDriverType() {
    final DriverType womenOnly = setupDriverType(1, "WOMEN_ONLY");
    final DriverType directConnect = setupDriverType(2, "DIRECT_CONNECT");
    when(driverTypeDslRepository.getAllEnabledOrdered()).thenReturn(ImmutableList.of(womenOnly, directConnect));
    testedInstance.refreshCache();

    final DriverType result = testedInstance.getDriverType("WOMEN_ONLY");

    assertEquals(womenOnly, result);
  }

  private DriverType setupDriverType(int bitmask, String category) {
    final DriverType driverType = new DriverType();
    driverType.setBitmask(bitmask);
    driverType.setName(category);
    driverType.setCityDriverTypes(ImmutableSet.of(createCityDriverType(driverType)));
    return driverType;
  }

  private CityDriverType createCityDriverType(DriverType driverType) {
    final CityDriverType cityDriverType = new CityDriverType();
    cityDriverType.setCityId(1L);
    cityDriverType.setBitmask(driverType.getBitmask());
    cityDriverType.setDriverType(driverType);
    return cityDriverType;
  }
}