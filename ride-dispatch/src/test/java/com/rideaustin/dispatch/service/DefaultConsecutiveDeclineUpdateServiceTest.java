package com.rideaustin.dispatch.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.ActiveDriverLocationService;
import com.rideaustin.service.ActiveDriversService;
import com.rideaustin.service.ActiveDriversService.GoOfflineEventSource;
import com.rideaustin.service.config.RideDispatchServiceConfig;
import com.rideaustin.service.event.EventsNotificationService;
import com.rideaustin.service.location.enums.LocationType;
import com.rideaustin.service.model.ConsecutiveDeclinedRequestsData;
import com.rideaustin.service.model.DispatchRequest;
import com.rideaustin.service.model.OnlineDriverDto;

public class DefaultConsecutiveDeclineUpdateServiceTest {

  @Mock
  private ActiveDriverLocationService activeDriverLocationService;
  @Mock
  private EventsNotificationService eventsNotificationService;
  @Mock
  private ActiveDriversService activeDriversService;
  @Mock
  private RideDispatchServiceConfig config;
  @Mock
  private DispatchDeclineRequestChecker checker;

  private DefaultConsecutiveDeclineUpdateService testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new DefaultConsecutiveDeclineUpdateService(activeDriverLocationService, eventsNotificationService,
      activeDriversService, config, Collections.singletonList(checker));
  }

  @Test
  public void processDriverDeclineDoesntDeactivateWhenAtLeastOneCheckerReturnsFalse() {
    final Ride ride = new Ride();
    ride.setRequestedCarType(new CarType());
    when(checker.checkIfActiveDriverNeedAddConsecutiveDeclineRequest(any(), eq(ride))).thenReturn(false);

    testedInstance.processDriverDecline(ride, new DispatchRequest(1L, 1L, 1L));

    verify(activeDriversService, never()).disableCarCategoryInActiveDriver(anyLong(), anyInt());
  }

  @Test
  public void processDriverDeclineDoesntDeactivateWhenDriverIsOffline() {
    final Ride ride = new Ride();
    ride.setRequestedCarType(new CarType());
    when(checker.checkIfActiveDriverNeedAddConsecutiveDeclineRequest(any(), eq(ride))).thenReturn(true);
    when(activeDriverLocationService.getById(anyLong(), eq(LocationType.ACTIVE_DRIVER))).thenReturn(null);

    testedInstance.processDriverDecline(ride, new DispatchRequest(1L, 1L, 1L));

    verify(activeDriversService, never()).disableCarCategoryInActiveDriver(anyLong(), anyInt());
  }

  @Test
  public void processDriverDeclineDoesntDeactivateWhenDeclineCountIsLessThanConfigured() {
    final Ride ride = new Ride();
    final CarType carType = new CarType();
    final String carCategory = "REGULAR";
    carType.setCarCategory(carCategory);
    ride.setRequestedCarType(carType);
    when(checker.checkIfActiveDriverNeedAddConsecutiveDeclineRequest(any(), eq(ride))).thenReturn(true);
    final OnlineDriverDto driverDto = new OnlineDriverDto();
    final ConsecutiveDeclinedRequestsData consecutiveDeclinedRequests = new ConsecutiveDeclinedRequestsData();
    consecutiveDeclinedRequests.put(carCategory, 0);
    driverDto.setConsecutiveDeclinedRequests(consecutiveDeclinedRequests);
    when(activeDriverLocationService.getById(anyLong(), eq(LocationType.ACTIVE_DRIVER))).thenReturn(driverDto);
    when(config.getDriverMaxDeclinedRequests()).thenReturn(3);

    testedInstance.processDriverDecline(ride, new DispatchRequest(1L, 1L, 1L));

    verify(activeDriversService, never()).disableCarCategoryInActiveDriver(anyLong(), anyInt());
  }

  @Test
  public void processDriverDeclineDeactivatesWhenDeclineCountIsEqualToConfigured() {
    final Ride ride = new Ride();
    final CarType carType = new CarType();
    final String carCategory = "REGULAR";
    carType.setCarCategory(carCategory);
    ride.setRequestedCarType(carType);
    when(checker.checkIfActiveDriverNeedAddConsecutiveDeclineRequest(any(), eq(ride))).thenReturn(true);
    final OnlineDriverDto driverDto = new OnlineDriverDto();
    final ConsecutiveDeclinedRequestsData consecutiveDeclinedRequests = new ConsecutiveDeclinedRequestsData();
    consecutiveDeclinedRequests.put(carCategory, 2);
    driverDto.setConsecutiveDeclinedRequests(consecutiveDeclinedRequests);
    when(activeDriverLocationService.getById(anyLong(), eq(LocationType.ACTIVE_DRIVER))).thenReturn(driverDto);
    when(config.getDriverMaxDeclinedRequests()).thenReturn(3);

    testedInstance.processDriverDecline(ride, new DispatchRequest(1L, 1L, 1L));

    verify(activeDriversService).disableCarCategoryInActiveDriver(anyLong(), anyInt());
  }

  @Test
  public void processDriverDeclineSendsGoOfflineWhenAllCategoriesDeactivated() throws ServerError {
    final Ride ride = new Ride();
    final CarType carType = new CarType();
    final String carCategory = "REGULAR";
    carType.setCarCategory(carCategory);
    ride.setRequestedCarType(carType);
    when(checker.checkIfActiveDriverNeedAddConsecutiveDeclineRequest(any(), eq(ride))).thenReturn(true);
    final OnlineDriverDto driverDto = new OnlineDriverDto();
    final ConsecutiveDeclinedRequestsData consecutiveDeclinedRequests = new ConsecutiveDeclinedRequestsData();
    consecutiveDeclinedRequests.put(carCategory, 2);
    driverDto.setConsecutiveDeclinedRequests(consecutiveDeclinedRequests);
    when(activeDriverLocationService.getById(anyLong(), eq(LocationType.ACTIVE_DRIVER))).thenReturn(driverDto);
    when(config.getDriverMaxDeclinedRequests()).thenReturn(3);
    when(activeDriversService.disableCarCategoryInActiveDriver(anyLong(), anyInt())).thenReturn(0);

    testedInstance.processDriverDecline(ride, new DispatchRequest(1L, 1L, 1L));

    verify(activeDriversService).deactivateWithMessage(anyLong(), anyLong(), eq(GoOfflineEventSource.MISSED_RIDES));
  }
}