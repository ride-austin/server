package com.rideaustin.dispatch.actions;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.rest.model.MobileDriverRideDto;
import com.rideaustin.service.model.States;
import com.rideaustin.utils.dispatch.StateMachineUtils;

public class DriverCancelActionTest extends BaseCancelActionTest<DriverCancelAction> {

  @Mock
  private ApplicationEventPublisher publisher;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    testedInstance = new DriverCancelAction();
    MockitoAnnotations.initMocks(this);

    setupSemaphore();
    requestContext.setRideId(RIDE_ID);
    StateMachineUtils.updateRequestContext(context, requestContext, persister, environment);

    setupDispatchCandidate();
    context.setSource(States.DRIVER_REACHED);
    when(ridePaymentConfig.isAsyncPreauthEnabled()).thenReturn(false);
  }

  @Override
  public void testCancelUnassignedRide() throws Exception {}

  @Override
  protected void setupStackedRide() {
    MobileDriverRideDto next = mock(MobileDriverRideDto.class);
    when(rideDslRepository.findNextRide(ACTIVE_DRIVER_ID)).thenReturn(next);
  }

  @Override
  protected void setupSingleRide() {
    when(rideDslRepository.findNextRide(ACTIVE_DRIVER_ID)).thenReturn(null);
  }

  @Override
  protected RideStatus getStatus() {
    return RideStatus.DRIVER_CANCELLED;
  }
}
