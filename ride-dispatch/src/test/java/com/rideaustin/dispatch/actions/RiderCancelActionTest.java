package com.rideaustin.dispatch.actions;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.dispatch.service.ConsecutiveDeclineUpdateService;
import com.rideaustin.dispatch.service.ConsecutiveDeclineUpdateServiceFactory;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.FareDetails;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.dsl.RideDriverDispatchDslRepository;
import com.rideaustin.rest.model.MobileRiderRideDto;
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.service.model.DispatchRequest;
import com.rideaustin.service.model.States;
import com.rideaustin.utils.dispatch.StateMachineUtils;

public class RiderCancelActionTest extends BaseCancelActionTest<RiderCancelAction> {

  private ReleaseRequestedDriversAction releaseRequestedDriversAction;
  @Mock
  private RideDriverDispatchDslRepository rideDriverDispatchDslRepository;
  @Mock
  private ConsecutiveDeclineUpdateService consecutiveDeclineUpdateService;
  @Mock
  private ConsecutiveDeclineUpdateServiceFactory consecutiveDeclineUpdateServiceFactory;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    releaseRequestedDriversAction = mock(ReleaseRequestedDriversAction.class);
    testedInstance = new RiderCancelAction(releaseRequestedDriversAction);
    MockitoAnnotations.initMocks(this);

    setupSemaphore();
    requestContext.setRideId(RIDE_ID);
    context.setSource(States.ACTIVE);

    StateMachineUtils.updateRequestContext(context, requestContext, persister, environment);
    when(ridePaymentConfig.isAsyncPreauthEnabled()).thenReturn(false);
  }

  @Test
  public void testCancelForcesDecline() throws Exception {
    context.setSource(States.DISPATCH_PENDING);
    Ride ride = new Ride();
    ride.setId(RIDE_ID);
    ride.setStatus(RideStatus.REQUESTED);
    when(rideDslRepository.findOne(RIDE_ID)).thenReturn(ride);
    when(fareService.processCancellation(eq(ride), anyBoolean())).thenReturn(Optional.of(new FareDetails()));

    DispatchCandidate candidate = new DispatchCandidate();
    candidate.setId(ACTIVE_DRIVER_ID);
    dispatchContext.setId(RIDE_ID);
    dispatchContext.setCandidate(candidate);
    StateMachineUtils.updateDispatchContext(context, dispatchContext, persister, environment);

    DispatchRequest dispatchRequest = mock(DispatchRequest.class);
    when(rideDriverDispatchDslRepository.findLastMissedRequestByRide(RIDE_ID)).thenReturn(dispatchRequest);

    when(consecutiveDeclineUpdateServiceFactory.createService(any())).thenReturn(consecutiveDeclineUpdateService);

    testedInstance.execute(context);

    verify(consecutiveDeclineUpdateService, times(1)).processDriverDecline(ride, dispatchRequest);
  }

  @Override
  protected void setupStackedRide() {
    when(rideDslRepository.findPrecedingRide(ACTIVE_DRIVER_ID)).thenReturn(new MobileRiderRideDto.PrecedingRide(0L, RideStatus.ACTIVE, "", "", 0.0, 0.0));
  }

  @Override
  protected void setupSingleRide() {
    when(rideDslRepository.findPrecedingRide(ACTIVE_DRIVER_ID)).thenReturn(null);
  }

  @Override
  protected RideStatus getStatus() {
    return RideStatus.RIDER_CANCELLED;
  }
}
