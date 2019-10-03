package com.rideaustin.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableList;
import com.rideaustin.repo.dsl.ActiveDriverDslRepository;
import com.rideaustin.service.location.ObjectLocationService;
import com.rideaustin.service.location.enums.LocationType;
import com.rideaustin.service.location.model.LocationObject;
import com.rideaustin.service.model.OnlineDriverDto;

public class DirectConnectSearchDriverHandlerTest {

  @Mock
  private ActiveDriverDslRepository activeDriverDslRepository;
  @Mock
  private ObjectLocationService<OnlineDriverDto> objectLocationService;

  private DirectConnectSearchDriverHandler testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new DirectConnectSearchDriverHandler(activeDriverDslRepository, objectLocationService);
  }

  @Test
  public void searchReturnsEmptyWithoutDCID() {
    final List<OnlineDriverDto> result = testedInstance.searchDrivers(new ActiveDriverSearchCriteria(34.186196, -97.984191, new ArrayList<>(), 0, "REGULAR", 1, 1L,
      1, 10, 10, null));

    assertTrue(result.isEmpty());
  }

  @Test
  public void searchReturnsEmptyWhenDriverIsNotFound() {
    final String directConnectId = "123";
    final ActiveDriverSearchCriteria searchCriteria = new ActiveDriverSearchCriteria(34.186196, -97.984191, new ArrayList<>(), 0, "REGULAR", 1, 1L,
      1, 10, 10, directConnectId);
    when(activeDriverDslRepository.findByDirectConnectId(eq(directConnectId), eq(searchCriteria.getDriverTypeBitmask())))
      .thenReturn(null);

    final List<OnlineDriverDto> result = testedInstance.searchDrivers(searchCriteria);

    assertTrue(result.isEmpty());
  }

  @Test
  public void searchReturnsEmptyWhenDriverIsIgnored() {
    final String directConnectId = "123";
    final ActiveDriverSearchCriteria searchCriteria = new ActiveDriverSearchCriteria(34.186196, -97.984191, new ArrayList<>(ImmutableList.of(1L)), 0, "REGULAR", 1, 1L,
      1, 10, 10, directConnectId);
    final OnlineDriverDto driverDto = new OnlineDriverDto();
    when(activeDriverDslRepository.findByDirectConnectId(eq(directConnectId), eq(searchCriteria.getDriverTypeBitmask())))
      .thenReturn(driverDto);

    final List<OnlineDriverDto> result = testedInstance.searchDrivers(searchCriteria);

    assertTrue(result.isEmpty());
  }

  @Test
  public void searchReturnsEmptyWhenDriverNotFoundInCache() {
    final String directConnectId = "123";
    final ActiveDriverSearchCriteria searchCriteria = new ActiveDriverSearchCriteria(34.186196, -97.984191, new ArrayList<>(), 0, "REGULAR", 1, 1L,
      1, 10, 10, directConnectId);
    final OnlineDriverDto driverDto = new OnlineDriverDto();
    when(activeDriverDslRepository.findByDirectConnectId(eq(directConnectId), eq(searchCriteria.getDriverTypeBitmask())))
      .thenReturn(driverDto);
    when(objectLocationService.getById(eq(driverDto.getId()), eq(LocationType.ACTIVE_DRIVER))).thenReturn(null);

    final List<OnlineDriverDto> result = testedInstance.searchDrivers(searchCriteria);

    assertTrue(result.isEmpty());
  }

  @Test
  public void searchReturnsFoundDriver() {
    final String directConnectId = "123";
    final ActiveDriverSearchCriteria searchCriteria = new ActiveDriverSearchCriteria(34.186196, -97.984191, new ArrayList<>(), 0, "REGULAR", 1, 1L,
      1, 10, 10, directConnectId);
    final OnlineDriverDto driverDto = new OnlineDriverDto();
    when(activeDriverDslRepository.findByDirectConnectId(eq(directConnectId), eq(searchCriteria.getDriverTypeBitmask())))
      .thenReturn(driverDto);
    final OnlineDriverDto cachedDriver = new OnlineDriverDto();
    final int availableCarCategoriesBitmask = 1;
    final int availableDriverTypesBitmask = 1;
    final double lat = 34.161656;
    final double lng = -97.4861981;
    cachedDriver.setAvailableCarCategoriesBitmask(availableCarCategoriesBitmask);
    cachedDriver.setAvailableDriverTypesBitmask(availableDriverTypesBitmask);
    cachedDriver.setLocationObject(new LocationObject(lat, lng));
    when(objectLocationService.getById(eq(driverDto.getId()), eq(LocationType.ACTIVE_DRIVER))).thenReturn(cachedDriver);

    final List<OnlineDriverDto> result = testedInstance.searchDrivers(searchCriteria);

    assertEquals(1, result.size());
    assertEquals(availableCarCategoriesBitmask, result.get(0).getAvailableCarCategoriesBitmask().intValue());
    assertEquals(availableDriverTypesBitmask, result.get(0).getAvailableDriverTypesBitmask().intValue());
    assertEquals(lat, result.get(0).getLatitude(), 0.0);
    assertEquals(lng, result.get(0).getLongitude(), 0.0);
  }
}