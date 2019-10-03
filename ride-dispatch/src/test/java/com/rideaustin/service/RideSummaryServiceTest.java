package com.rideaustin.service;

import static com.rideaustin.test.util.TestUtils.mockCarType;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Optional;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.config.RideJobServiceConfig;
import com.rideaustin.service.notification.RideFlowPushNotificationFacade;

public class RideSummaryServiceTest {

  @Mock
  private RideTrackerService rideTrackerService;
  @Mock
  private RideDslRepository rideDslRepository;
  @Mock
  private RideFlowPushNotificationFacade pushNotificationsFacade;
  @Mock
  private SchedulerService schedulerService;
  @Mock
  private RideJobServiceConfig config;
  @Mock
  private FareService fareService;

  private Ride ride;

  private RideSummaryService testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new RideSummaryService(rideTrackerService, rideDslRepository, pushNotificationsFacade, fareService,
    schedulerService, config);

    ride = new Ride();
    ride.setId(new Random().nextLong());
    ride.setDriverReachedOn(new Date());
    ride.setCreatedDate(new Date());
    ride.setDriverAcceptedOn(new Date());
    ride.setStatus(RideStatus.COMPLETED);
    when(rideDslRepository.findOne(anyLong())).thenReturn(ride);
    when(rideDslRepository.save(any())).thenReturn(ride);
  }

  @Test
  public void testCompleteRide() throws RideAustinException {
    ride.setRequestedCarType(mockCarType());
    when(fareService.calculateFinalFare(any(), any())).thenReturn(Optional.empty());

    testedInstance.completeRide(ride.getId());

    verify(rideTrackerService).saveStaticImage(ride);
    verify(pushNotificationsFacade).sendRideUpdateToRider(ride.getId());
  }

}