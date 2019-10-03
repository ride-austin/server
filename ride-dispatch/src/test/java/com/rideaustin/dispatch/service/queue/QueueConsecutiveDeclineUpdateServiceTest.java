package com.rideaustin.dispatch.service.queue;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.dispatch.service.DispatchDeclineRequestChecker;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.service.ActiveDriverLocationService;
import com.rideaustin.service.ActiveDriversService;
import com.rideaustin.service.areaqueue.AreaQueuePenaltyService;
import com.rideaustin.service.config.AreaQueueConfig;
import com.rideaustin.service.config.RideDispatchServiceConfig;
import com.rideaustin.service.event.EventsNotificationService;
import com.rideaustin.service.location.enums.LocationType;
import com.rideaustin.service.model.ConsecutiveDeclinedRequestsData;
import com.rideaustin.service.model.DispatchRequest;
import com.rideaustin.service.model.OnlineDriverDto;

public class QueueConsecutiveDeclineUpdateServiceTest {

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
  @Mock
  private AreaQueueConfig areaQueueConfig;
  @Mock
  private AreaQueuePenaltyService penaltyService;

  private QueueConsecutiveDeclineUpdateService testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new QueueConsecutiveDeclineUpdateService(activeDriverLocationService, eventsNotificationService,
    activeDriversService, config, Collections.singletonList(checker), areaQueueConfig, penaltyService);
  }

  @Test
  public void processDriverDeclinePenalizesQueuedDrivers() {
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
    when(areaQueueConfig.getMaxDeclines()).thenReturn(3);

    testedInstance.processDriverDecline(ride, new DispatchRequest(1L, 1L, 1L));

    verify(penaltyService).penalize(anyLong());
  }
}