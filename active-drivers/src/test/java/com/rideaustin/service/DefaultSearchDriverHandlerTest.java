package com.rideaustin.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.rideaustin.model.Area;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.ride.DriverType;
import com.rideaustin.repo.dsl.ActiveDriverDslRepository;
import com.rideaustin.service.areaqueue.AreaQueueEntryService;
import com.rideaustin.service.config.ActiveDriverServiceConfig;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.service.user.DriverTypeCache;

public class DefaultSearchDriverHandlerTest {

  @Mock
  private ActiveDriverServiceConfig config;
  @Mock
  private DriverTypeCache driverTypeCache;
  @Mock
  private ActiveDriverDslRepository activeDriverDslRepository;
  @Mock
  private ActiveDriverLocationService activeDriverLocationService;
  @Mock
  private AreaQueueEntryService areaQueueEntryService;
  @Mock
  private UpdateDistanceTimeService updateDistanceTimeService;

  private DefaultSearchDriverHandler testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    when(config.getFingerprintedDriverHandicap()).thenReturn(10);
    when(driverTypeCache.toBitMask(Collections.singleton(DriverType.FINGERPRINTED))).thenReturn(4);

    testedInstance = new DefaultSearchDriverHandler(config, driverTypeCache, activeDriverDslRepository, activeDriverLocationService,
      areaQueueEntryService, updateDistanceTimeService);
  }

  @Test
  public void searchDriversByDistanceFiltersIgnoredDrivers() {
    final OnlineDriverDto firstDriver = new OnlineDriverDto(1L, ActiveDriverStatus.AVAILABLE, 1L, 1L, "A B", "+15125555555", "AZAZA");
    firstDriver.setAvailableCarCategoriesBitmask(1);
    final OnlineDriverDto secondDriver = new OnlineDriverDto(2L, ActiveDriverStatus.AVAILABLE, 2L, 2L, "B C", "+15125555556", "AZAZB");
    secondDriver.setAvailableCarCategoriesBitmask(1);
    when(activeDriverLocationService.locationAround(anyDouble(), anyDouble(), anyInt(), anyString(), anyInt()))
      .thenReturn(new ArrayList<>(ImmutableList.of(
        firstDriver,
        secondDriver
      )));

    final List<OnlineDriverDto> result = testedInstance.searchDrivers(new ActiveDriverSearchCriteria(34.48618, -97.68164, ImmutableList.of(1L), 1, "REGULAR",
      1, 1L, null, 10, 10));

    assertEquals(1, result.size());
    assertEquals(2L, result.get(0).getId());
  }

  @Test
  public void searchDriverByDistanceFiltersDriverType() {
    final OnlineDriverDto firstDriver = new OnlineDriverDto(1L, ActiveDriverStatus.AVAILABLE, 1L, 1L, "A B", "+15125555555", "AZAZA");
    firstDriver.setAvailableCarCategoriesBitmask(1);
    final OnlineDriverDto secondDriver = new OnlineDriverDto(2L, ActiveDriverStatus.AVAILABLE, 2L, 2L, "B C", "+15125555556", "AZAZB");
    secondDriver.setAvailableDriverTypesBitmask(1);
    secondDriver.setAvailableCarCategoriesBitmask(1);
    when(activeDriverLocationService.locationAround(anyDouble(), anyDouble(), anyInt(), anyString(), anyInt()))
      .thenReturn(new ArrayList<>(ImmutableList.of(
        firstDriver,
        secondDriver
      )));
    when(activeDriverDslRepository.findDriverTypeGrantedActiveDriverIds(anyListOf(Long.class)))
      .thenReturn(ImmutableMap.of(
        2L, 1
      ));

    final List<OnlineDriverDto> result = testedInstance.searchDrivers(new ActiveDriverSearchCriteria(34.48618, -97.68164, Collections.emptyList(), 1, "REGULAR",
      1, 1L, 1, 10, 10));

    assertEquals(1, result.size());
    assertEquals(2L, result.get(0).getId());
  }

  @Test
  public void searchDriverByDistanceFiltersCarType() {
    final OnlineDriverDto firstDriver = new OnlineDriverDto(1L, ActiveDriverStatus.AVAILABLE, 1L, 1L, "A B", "+15125555555", "AZAZA");
    firstDriver.setAvailableCarCategoriesBitmask(1);
    final OnlineDriverDto secondDriver = new OnlineDriverDto(2L, ActiveDriverStatus.AVAILABLE, 2L, 2L, "B C", "+15125555556", "AZAZB");
    secondDriver.setAvailableCarCategoriesBitmask(2);
    when(activeDriverLocationService.locationAround(anyDouble(), anyDouble(), anyInt(), anyString(), anyInt()))
      .thenReturn(new ArrayList<>(ImmutableList.of(
        firstDriver,
        secondDriver
      )));

    final List<OnlineDriverDto> result = testedInstance.searchDrivers(new ActiveDriverSearchCriteria(34.48618, -97.68164, Collections.emptyList(), 1, "REGULAR",
      2, 1L, null, 10, 10));

    assertEquals(1, result.size());
    assertEquals(2L, result.get(0).getId());
  }

  @Test
  public void searchDriverInQueueFiltersDriverType() {
    final OnlineDriverDto firstDriver = new OnlineDriverDto(1L, ActiveDriverStatus.AVAILABLE, 1L, 1L, "A B", "+15125555555", "AZAZA");
    firstDriver.setAvailableCarCategoriesBitmask(1);
    final OnlineDriverDto secondDriver = new OnlineDriverDto(2L, ActiveDriverStatus.AVAILABLE, 2L, 2L, "B C", "+15125555556", "AZAZB");
    secondDriver.setAvailableDriverTypesBitmask(1);
    secondDriver.setAvailableCarCategoriesBitmask(1);
    when(areaQueueEntryService.getAvailableActiveDriversFromArea(any(Area.class), anyListOf(Long.class), anyString()))
      .thenReturn(new ArrayList<>(ImmutableList.of(
        firstDriver,
        secondDriver
      )));
    when(activeDriverDslRepository.findDriverTypeGrantedActiveDriverIds(anyListOf(Long.class)))
      .thenReturn(ImmutableMap.of(
        2L, 1
      ));

    final List<OnlineDriverDto> result = testedInstance.searchDrivers(new QueuedActiveDriverSearchCriteria(
      new Area(), Collections.emptyList(), "REGULAR", 1
    ));

    assertEquals(1, result.size());
    assertEquals(2L, result.get(0).getId());
  }
}