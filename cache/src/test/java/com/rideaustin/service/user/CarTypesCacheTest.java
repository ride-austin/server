package com.rideaustin.service.user;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.ride.CityCarType;
import com.rideaustin.repo.dsl.CarTypeDslRepository;

public class CarTypesCacheTest {

  @Mock
  private CarTypeDslRepository carTypeDslRepository;

  private CarTypesCache testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new CarTypesCache(carTypeDslRepository);
  }

  @Test
  public void toBitmaskSumsCarTypeBitmasks() {
    final CarType regular = setupCarType(1, "REGULAR");
    final CarType suv = setupCarType(2, "SUV");
    when(carTypeDslRepository.getAllOrdered()).thenReturn(ImmutableList.of(regular, suv));
    testedInstance.refreshCache();

    final int result = testedInstance.toBitMask(ImmutableSet.of("REGULAR", "SUV"));

    assertEquals(3, result);
  }

  @Test
  public void fromBitmaskCollectsCarCategories() {
    final CarType regular = setupCarType(1, "REGULAR");
    final CarType suv = setupCarType(2, "SUV");
    when(carTypeDslRepository.getAllOrdered()).thenReturn(ImmutableList.of(regular, suv));
    testedInstance.refreshCache();

    final Set<String> result = testedInstance.fromBitMask(regular.getBitmask() | suv.getBitmask());

    assertTrue(CollectionUtils.isEqualCollection(result, ImmutableSet.of("REGULAR", "SUV")));
  }

  @Test
  public void getCityCarTypeReturnsMatchingCityCarType() {
    final CarType regular = setupCarType(1, "REGULAR");
    final CarType suv = setupCarType(2, "SUV");
    when(carTypeDslRepository.getAllOrdered()).thenReturn(ImmutableList.of(regular, suv));
    testedInstance.refreshCache();

    final CityCarType result = testedInstance.getCityCarType(1L, "SUV");

    assertEquals(suv, result.getCarType());
  }

  @Test
  public void getCityCarTypeReturnsNullWhenNoneMatchingCityCarType() {
    final CarType regular = setupCarType(1, "REGULAR");
    final CarType suv = setupCarType(2, "SUV");
    when(carTypeDslRepository.getAllOrdered()).thenReturn(ImmutableList.of(regular, suv));
    testedInstance.refreshCache();

    final CityCarType result = testedInstance.getCityCarType(1L, "PREMIUM");

    assertNull(result);
  }

  private CarType setupCarType(int bitmask, String category) {
    final CarType carType = new CarType();
    carType.setBitmask(bitmask);
    carType.setCarCategory(category);
    carType.setCityCarTypes(ImmutableSet.of(createCityCarType(carType)));
    return carType;
  }

  private CityCarType createCityCarType(CarType carType) {
    final CityCarType cityCarType = new CityCarType();
    cityCarType.setCityId(1L);
    cityCarType.setCarType(carType);
    return cityCarType;
  }

}