package com.rideaustin.dispatch.actions;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.statemachine.action.Action;

import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.enums.EventType;
import com.rideaustin.model.enums.PaymentStatus;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.FareDetails;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.ride.RideTracker;
import com.rideaustin.repo.dsl.ActiveDriverDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.service.ActiveDriverLocationService;
import com.rideaustin.service.FareService;
import com.rideaustin.service.RequestedDriversRegistry;
import com.rideaustin.service.RideTrackerService;
import com.rideaustin.service.SchedulerService;
import com.rideaustin.service.StackedDriverRegistry;
import com.rideaustin.service.config.RidePaymentConfig;
import com.rideaustin.service.event.EventsNotificationService;
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.notification.RideFlowPushNotificationFacade;
import com.rideaustin.service.ride.jobs.ProcessRidePaymentJob;
import com.rideaustin.service.thirdparty.StripeService;
import com.rideaustin.utils.dispatch.StateMachineUtils;

public abstract class BaseCancelActionTest<T extends Action<States, Events>> extends PersistingContextSupport {

  protected static final long RIDE_ID = 1L;
  protected static final long ACTIVE_DRIVER_ID = 2L;

  @Mock
  protected RideFlowPushNotificationFacade pushNotificationsFacade;
  @Mock
  protected RideDslRepository rideDslRepository;
  @Mock
  protected ActiveDriverDslRepository activeDriverDslRepository;
  @Mock
  protected RequestedDriversRegistry requestedDriversRegistry;
  @Mock
  protected StackedDriverRegistry stackedDriverRegistry;
  @Mock
  protected ActiveDriverLocationService activeDriverLocationService;

  @Mock
  protected StripeService stripeService;
  @Mock
  protected RideTrackerService rideTrackerService;
  @Mock
  protected FareService fareService;
  @Mock
  protected SchedulerService schedulerService;
  @Mock
  protected EventsNotificationService eventsNotificationService;
  @Mock
  protected DumpContextAction dumpContextAction;
  @Mock
  protected RedissonClient redissonClient;
  @Mock
  protected RidePaymentConfig ridePaymentConfig;

  @InjectMocks
  protected T testedInstance;

  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  protected void setupSemaphore() throws InterruptedException {
    RSemaphore semaphore = mock(RSemaphore.class);
    when(redissonClient.getSemaphore(anyString())).thenReturn(semaphore);
    when(semaphore.tryAcquire(anyLong(), eq(TimeUnit.MILLISECONDS))).thenReturn(true);
  }

  @Test
  public void testExecuteReturnsIfNoRideIsPresent() throws Exception {
    when(rideDslRepository.findOneWithRider(RIDE_ID)).thenReturn(null);

    testedInstance.execute(context);

    verify(stripeService, never()).refundPreCharge(any(Ride.class));
  }

  @Test
  public void testExecuteReturnsIfRideIsAlreadyInCancelledStatus() throws Exception {
    Ride alreadyCancelledRide = getAlreadyCancelledRide();
    when(rideDslRepository.findOneWithRider(RIDE_ID)).thenReturn(alreadyCancelledRide);

    testedInstance.execute(context);

    verify(stripeService, never()).refundPreCharge(any(Ride.class));
  }

  @Test
  public void testPreChargeIsRefunded() throws Exception {
    Ride ride = new Ride();
    ride.setStatus(RideStatus.ACTIVE);
    when(rideDslRepository.findOneWithRider(RIDE_ID)).thenReturn(ride);
    when(fareService.processCancellation(eq(ride), anyBoolean())).thenReturn(Optional.of(new FareDetails()));

    testedInstance.execute(context);

    verify(stripeService, times(1)).refundPreCharge(any(Ride.class));
  }

  @Test
  public void testLastRideTrackerIsSaved() throws Exception {
    Ride ride = new Ride();
    ride.setId(RIDE_ID);
    ride.setStatus(RideStatus.ACTIVE);
    when(rideDslRepository.findOneWithRider(RIDE_ID)).thenReturn(ride);
    when(fareService.processCancellation(eq(ride), anyBoolean())).thenReturn(Optional.of(new FareDetails()));

    testedInstance.execute(context);

    verify(rideTrackerService, times(1)).updateRideTracker(eq(RIDE_ID), any(RideTracker.class));
    verify(rideTrackerService, times(1)).saveStaticImage(ride);
  }

  @Test
  public void testPaymentProcessingIsTriggeredIfNeeded() throws Exception {
    Ride ride = new Ride();
    ride.setId(RIDE_ID);
    ride.setStatus(RideStatus.ACTIVE);
    when(rideDslRepository.findOneWithRider(RIDE_ID)).thenReturn(ride);
    when(fareService.processCancellation(eq(ride), anyBoolean())).thenReturn(Optional.of(new FareDetails()));
    when(fareService.shouldChargeCancellationFee(ride, getStatus())).thenReturn(true);
    setupDispatchCandidate();

    testedInstance.execute(context);

    verify(schedulerService, times(1)).triggerJob(eq(ProcessRidePaymentJob.class),
      eq(String.valueOf(RIDE_ID)), eq("RidePayment"), eq(10),
      eq(Collections.singletonMap("rideId", RIDE_ID)));
  }

  @Test
  public void testCancelUnassignedRide() throws Exception {
    Ride ride = new Ride();
    ride.setId(RIDE_ID);
    ride.setStatus(RideStatus.REQUESTED);
    when(rideDslRepository.findOneWithRider(RIDE_ID)).thenReturn(ride);
    when(fareService.processCancellation(eq(ride), anyBoolean())).thenReturn(Optional.of(new FareDetails()));

    testedInstance.execute(context);

    verify(requestedDriversRegistry, never()).isRequested(anyLong());
    verifyCancelledStatus();
  }

  @Test
  public void testCancelRequestedRideWithDispatchCandidate() throws Exception {
    Ride ride = new Ride();
    ride.setId(RIDE_ID);
    ride.setStatus(RideStatus.REQUESTED);
    when(rideDslRepository.findOneWithRider(RIDE_ID)).thenReturn(ride);
    when(fareService.processCancellation(eq(ride), anyBoolean())).thenReturn(Optional.of(new FareDetails()));

    DispatchCandidate candidate = setupDispatchCandidate();

    when(requestedDriversRegistry.isRequested(ACTIVE_DRIVER_ID)).thenReturn(true);

    testedInstance.execute(context);

    verify(eventsNotificationService, times(1)).sendRideUpdateToDriver(ride, candidate, EventType.from(getStatus()));
    verify(requestedDriversRegistry, times(1)).remove(ACTIVE_DRIVER_ID);
  }

  @Test
  public void testCancelSingleAssignedRide() throws Exception {
    testCancelSingleRide(RideStatus.DRIVER_ASSIGNED);
  }

  @Test
  public void testCancelStackedAssignedRide() throws Exception {
    testCancelStackedRide(RideStatus.DRIVER_ASSIGNED);
    verifyDriverStackable();
  }

  @Test
  public void testCancelReachedSingleRide() throws Exception {
    testCancelSingleRide(RideStatus.DRIVER_REACHED);
  }

  @Test
  public void testCancelReachedStackedRide() throws Exception {
    testCancelStackedRide(RideStatus.DRIVER_REACHED);
    verifyDriverReleased();
  }

  @Test
  public void testCancelAssignedStackedRideDoesntChargeFee() throws Exception {
    testCancelStackedRide(RideStatus.DRIVER_ASSIGNED);
    verify(fareService, times(1)).processCancellation(any(), eq(false));
  }

  @Test
  public void testCancelReachedStackedRideChargeFee() throws Exception {
    when(fareService.shouldChargeCancellationFee(any(), eq(getStatus()))).thenReturn(true);
    testCancelStackedRide(RideStatus.DRIVER_REACHED);
    verify(fareService, times(1)).processCancellation(any(), eq(true));
  }

  @Test
  public void testCancelReachedSingleRideDoesntChargeFeeWhenChargeIsScheduled() throws Exception {
    when(fareService.shouldChargeCancellationFee(any(), eq(getStatus()))).thenReturn(true);
    when(schedulerService.checkIfExists(eq(String.valueOf(RIDE_ID)), anyString())).thenReturn(true);
    testCancelStackedRide(RideStatus.DRIVER_REACHED);
    verify(schedulerService, never()).triggerJob(eq(ProcessRidePaymentJob.class),
      eq(String.valueOf(RIDE_ID)), anyString(), anyInt(), anyMap());
  }

  @Test
  public void testCancelReachedSingleRideChargeFeeWhenChargeIsNotScheduled() throws Exception {
    when(fareService.shouldChargeCancellationFee(any(), eq(getStatus()))).thenReturn(true);
    when(schedulerService.checkIfExists(eq(String.valueOf(RIDE_ID)), anyString())).thenReturn(false);
    testCancelStackedRide(RideStatus.DRIVER_REACHED);
    verify(schedulerService, times(1)).triggerJob(eq(ProcessRidePaymentJob.class),
      eq(String.valueOf(RIDE_ID)), anyString(), anyInt(), anyMap());
  }

  protected abstract void setupStackedRide();

  protected abstract void setupSingleRide();

  protected Ride getAlreadyCancelledRide() {
    Ride ride = new Ride();
    ride.setStatus(getStatus());
    return ride;
  }

  protected abstract RideStatus getStatus();

  protected void testCancelSingleRide(RideStatus status) {
    Ride ride = new Ride();
    ride.setId(RIDE_ID);
    ride.setStatus(status);
    when(rideDslRepository.findOneWithRider(RIDE_ID)).thenReturn(ride);
    when(fareService.processCancellation(eq(ride), anyBoolean())).thenReturn(Optional.of(new FareDetails()));

    DispatchCandidate candidate = setupDispatchCandidate();

    setupSingleRide();

    testedInstance.execute(context);

    verify(eventsNotificationService, times(1)).sendRideUpdateToDriver(ride, candidate, EventType.from(getStatus()));
    verifyCancelledStatus();
    verifyDriverReleased();
  }

  protected DispatchCandidate setupDispatchCandidate() {
    DispatchCandidate candidate = new DispatchCandidate();
    candidate.setId(ACTIVE_DRIVER_ID);
    dispatchContext.setCandidate(candidate);
    StateMachineUtils.updateDispatchContext(context, dispatchContext, persister, environment);
    return candidate;
  }

  protected void testCancelStackedRide(RideStatus status) {
    Ride ride = new Ride();
    ride.setId(RIDE_ID);
    ride.setStatus(status);
    when(rideDslRepository.findOneWithRider(RIDE_ID)).thenReturn(ride);
    when(fareService.processCancellation(eq(ride), anyBoolean())).thenReturn(Optional.of(new FareDetails()));

    DispatchCandidate candidate = setupDispatchCandidate();

    setupStackedRide();

    testedInstance.execute(context);

    verify(eventsNotificationService, times(1)).sendRideUpdateToDriver(ride, candidate, EventType.from(getStatus()));
    verifyCancelledStatus();
  }

  private void verifyCancelledStatus() {
    verify(rideDslRepository, times(1)).cancelRide(eq(RIDE_ID),
      eq(getStatus()), any(FareDetails.class), any(PaymentStatus.class));
  }

  private void verifyDriverStackable() {
    verify(stackedDriverRegistry, times(1)).makeStackable(ACTIVE_DRIVER_ID);
  }

  protected void verifyDriverReleased() {
    verify(activeDriverDslRepository, times(1)).setRidingDriverAsAvailable(ACTIVE_DRIVER_ID);
    verify(activeDriverLocationService, times(1)).updateActiveDriverLocationStatus(ACTIVE_DRIVER_ID, ActiveDriverStatus.AVAILABLE);
  }
}
