package com.rideaustin.service;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.model.enums.ConfigurationWeekday;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.ride.CityCarType;
import com.rideaustin.repo.dsl.CarTypeDslRepository;
import com.rideaustin.service.user.CarTypesCache;

public class CarTypeServiceTest {

  private ObjectMapper mapper = new ObjectMapper();
  @Mock
  private CarTypesCache cache;
  @Mock
  private CarTypeDslRepository repository;

  private CarTypeService testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new CarTypeService(mapper, cache, repository);
  }

  @Test
  public void getCityCarTypesReturnsTypeActiveToday() {
    final long cityId = 1L;

    final Calendar calendar = Calendar.getInstance();
    final int today = calendar.get(Calendar.DAY_OF_WEEK);
    final int todayBitmask = ConfigurationWeekday.fromWeekday(today).getBitmask();

    final CarType carType = new CarType();
    final CityCarType cityCarType = new CityCarType();
    carType.setConfiguration("{}");
    cityCarType.setConfiguration(String.format("{\"activeWeekDays\":%d,\"activeFrom\":0,\"activeTo\":24}", todayBitmask));
    cityCarType.setCarType(carType);
    when(cache.getCityCarTypes(eq(cityId))).thenReturn(Collections.singletonList(cityCarType));

    final List<CityCarType> result = testedInstance.getCityCarTypes(cityId);

    assertEquals(1, result.size());
    assertEquals(cityCarType, result.get(0));
  }

  @Test
  public void getCityCarTypesReturnsAllTypes() {
    final long cityId = 1L;

    final CarType carType = new CarType();
    final CityCarType cityCarType = new CityCarType();
    carType.setConfiguration("{}");
    cityCarType.setConfiguration("{}");
    cityCarType.setCarType(carType);
    when(cache.getCityCarTypes(eq(cityId))).thenReturn(Collections.singletonList(cityCarType));

    final List<CityCarType> result = testedInstance.getCityCarTypes(cityId);

    assertEquals(1, result.size());
    assertEquals(cityCarType, result.get(0));
  }

  @Test
  public void getCityCarTypeWithFallbackReturnsCached() {
    final long cityId = 1L;
    final String category = "REGULAR";

    final CarType carType = new CarType();
    final CityCarType cityCarType = new CityCarType();
    carType.setConfiguration("{}");
    carType.setCarCategory(category);
    cityCarType.setConfiguration("{}");
    cityCarType.setCarType(carType);
    when(cache.getCityCarTypes(eq(cityId))).thenReturn(Collections.singletonList(cityCarType));

    final Optional<CityCarType> result = testedInstance.getCityCarTypeWithFallback(category, cityId);

    assertTrue(result.isPresent());
    assertEquals(category, result.get().getCarType().getCarCategory());
    verify(repository, never()).findByTypeAndCity(anyString(), anyLong());
  }

  @Test
  public void getCityCarTypeWithFallbackReturnsFromDatabase() {
    final long cityId = 1L;
    final String category = "REGULAR";

    final CarType carType = new CarType();
    final CityCarType cityCarType = new CityCarType();
    carType.setConfiguration("{}");
    carType.setCarCategory(category);
    cityCarType.setConfiguration("{}");
    cityCarType.setCarType(carType);
    when(cache.getCityCarTypes(eq(cityId))).thenReturn(Collections.emptyList());
    when(repository.findByTypeAndCity(anyString(), anyLong())).thenReturn(Optional.of(cityCarType));

    final Optional<CityCarType> result = testedInstance.getCityCarTypeWithFallback(category, cityId);

    assertTrue(result.isPresent());
    assertEquals(category, result.get().getCarType().getCarCategory());
    verify(repository, times(1)).findByTypeAndCity(anyString(), anyLong());
  }

}