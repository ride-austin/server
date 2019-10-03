package com.rideaustin.dispatch.actions;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.FareDetails;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.rest.model.MobileDriverRideDto;
import com.rideaustin.rest.model.MobileRiderRideDto;
import com.rideaustin.service.ride.jobs.ProcessRidePaymentJob;
import com.rideaustin.utils.dispatch.StateMachineUtils;

public class AdminCancelActionTest extends BaseCancelActionTest<AdminCancelAction> {

  private boolean noNext;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    testedInstance = new AdminCancelAction();
    MockitoAnnotations.initMocks(this);

    setupSemaphore();
    requestContext.setRideId(RIDE_ID);
    when(ridePaymentConfig.isAsyncPreauthEnabled()).thenReturn(false);

    StateMachineUtils.updateRequestContext(context, requestContext, persister, environment);
    noNext = true;
  }

  @Test
  @Override
  public void testPaymentProcessingIsTriggeredIfNeeded() throws Exception {
    Ride ride = new Ride();
    ride.setId(RIDE_ID);
    ride.setStatus(RideStatus.ACTIVE);
    when(rideDslRepository.findOne(RIDE_ID)).thenReturn(ride);
    when(fareService.processCancellation(eq(ride), anyBoolean())).thenReturn(Optional.of(new FareDetails()));

    testedInstance.execute(context);

    verify(schedulerService, never()).triggerJob(eq(ProcessRidePaymentJob.class),
      eq(String.valueOf(RIDE_ID)), eq("RidePayment"), eq(10),
      eq(Collections.singletonMap("rideId", RIDE_ID)));
  }

  @Test
  @Override
  public void testCancelReachedStackedRideChargeFee() throws Exception {
    when(fareService.shouldChargeCancellationFee(any(), eq(getStatus()))).thenReturn(true);
    testCancelStackedRide(RideStatus.DRIVER_REACHED);
    verify(fareService, times(1)).processCancellation(any(), eq(false));
  }

  @Test
  @Override
  public void testCancelReachedSingleRideDoesntChargeFeeWhenChargeIsScheduled() {
    assertTrue(true);
  }

  @Test
  @Override
  public void testCancelReachedSingleRideChargeFeeWhenChargeIsNotScheduled() {
    assertTrue(true);
  }

  @Test
  public void testRiderIsNotified() throws Exception {
    Ride ride = new Ride();
    ride.setId(RIDE_ID);
    ride.setStatus(RideStatus.ACTIVE);
    when(rideDslRepository.findOneWithRider(RIDE_ID)).thenReturn(ride);
    when(fareService.processCancellation(eq(ride), anyBoolean())).thenReturn(Optional.of(new FareDetails()));

    testedInstance.execute(context);

    verify(pushNotificationsFacade, times(1)).sendRideUpdateToRider(RIDE_ID, RideStatus.ADMIN_CANCELLED);
  }

  @Test
  public void testCancelActiveSingleRide() throws Exception {
    testCancelSingleRide(RideStatus.ACTIVE);
  }

  @Test
  public void testCancelActiveStackedRide() throws Exception {
    noNext = false;
    MobileDriverRideDto next = mock(MobileDriverRideDto.class);
    when(rideDslRepository.findNextRide(ACTIVE_DRIVER_ID)).thenReturn(next);
    testCancelStackedRide(RideStatus.ACTIVE);
    verify(stackedDriverRegistry, times(1)).removeFromStack(ACTIVE_DRIVER_ID);
  }

  @Override
  protected void setupSingleRide() {
    when(rideDslRepository.findNextRide(ACTIVE_DRIVER_ID)).thenReturn(null);
    when(rideDslRepository.findPrecedingRide(ACTIVE_DRIVER_ID)).thenReturn(null);
  }

  @Override
  protected void setupStackedRide() {
    if (noNext) {
      when(rideDslRepository.findNextRide(ACTIVE_DRIVER_ID)).thenReturn(null);
    }
    when(rideDslRepository.findPrecedingRide(ACTIVE_DRIVER_ID)).thenReturn(new MobileRiderRideDto.PrecedingRide(0L, RideStatus.ACTIVE, "", "", 0.0, 0.0));
  }

  @Override
  protected RideStatus getStatus() {
    return RideStatus.ADMIN_CANCELLED;
  }
}