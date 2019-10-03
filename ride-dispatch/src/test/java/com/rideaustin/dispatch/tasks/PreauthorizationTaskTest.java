package com.rideaustin.dispatch.tasks;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.messaging.MessageHeaders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.dispatch.service.RideFlowStateMachineProvider;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.RidePreauthorizationService;
import com.rideaustin.service.model.Events;
import com.rideaustin.utils.RandomString;

public class PreauthorizationTaskTest {

  private ObjectMapper mapper = new ObjectMapper();
  @Mock
  private RideDslRepository rideDslRepository;
  @Mock
  private RidePreauthorizationService preauthorizationService;
  @Mock
  private RideFlowStateMachineProvider machineProvider;
  @Mock
  private RedissonClient redissonClient;
  @Mock
  private RSemaphore semaphore;

  private PreauthorizationTask testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new PreauthorizationTask(mapper, rideDslRepository, preauthorizationService, machineProvider,
      redissonClient);
  }

  @Test
  public void runAbortsOnNullRideId() throws RideAustinException {
    testedInstance.run();

    verify(preauthorizationService, never()).preauthorizeRide(any(Ride.class), any(CarType.Configuration.class),
      anyString(), any(Runnable.class));
  }

  @Test
  public void runAbortsOnNotNullPreCharge() throws RideAustinException {
    final long rideId = 1L;
    testedInstance.withRideId(rideId);

    final Ride ride = new Ride();
    ride.setPreChargeId("153");
    when(rideDslRepository.findOneWithRider(eq(rideId))).thenReturn(ride);

    testedInstance.run();

    verify(preauthorizationService, never()).preauthorizeRide(any(Ride.class), any(CarType.Configuration.class),
      anyString(), any(Runnable.class));
  }

  @Test
  public void runSetsPrechargeIdOnSuccess() throws RideAustinException {
    final long rideId = 1L;
    testedInstance.withRideId(rideId);

    final Ride ride = new Ride();
    final String chargeId = RandomString.generate();
    final CarType requestedCarType = new CarType();
    requestedCarType.setConfiguration("{}");
    ride.setRequestedCarType(requestedCarType);
    when(rideDslRepository.findOneWithRider(eq(rideId))).thenReturn(ride);
    when(redissonClient.getSemaphore(anyString())).thenReturn(semaphore);
    when(preauthorizationService.preauthorizeRide(any(Ride.class), any(CarType.Configuration.class),
      anyString(), any(Runnable.class))).thenReturn(chargeId);

    testedInstance.run();

    verify(rideDslRepository, times(1)).setPrechargeId(eq(rideId), eq(chargeId));
    verify(semaphore, times(1)).release(1);
  }

  @Test
  public void runSendsEventOnFailure() throws RideAustinException {
    final long rideId = 1L;
    testedInstance.withRideId(rideId);

    final Ride ride = new Ride();
    final CarType requestedCarType = new CarType();
    requestedCarType.setConfiguration("{}");
    ride.setRequestedCarType(requestedCarType);
    when(rideDslRepository.findOneWithRider(eq(rideId))).thenReturn(ride);
    when(redissonClient.getSemaphore(anyString())).thenReturn(semaphore);
    when(preauthorizationService.preauthorizeRide(any(Ride.class), any(CarType.Configuration.class),
      anyString(), any(Runnable.class))).thenThrow(new BadRequestException(""));

    testedInstance.run();

    verify(rideDslRepository, never()).setPrechargeId(anyLong(), anyString());
    verify(machineProvider, times(1)).sendProxiedEvent(eq(rideId), eq(Events.ABORT_PREAUTHORIZATION_FAILED),
      any(MessageHeaders.class));
    verify(semaphore, times(1)).release(1);
  }
}