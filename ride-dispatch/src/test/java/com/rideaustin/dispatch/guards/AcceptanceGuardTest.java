package com.rideaustin.dispatch.guards;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.dispatch.actions.PersistingContextSupport;
import com.rideaustin.model.enums.DispatchStatus;
import com.rideaustin.model.ride.RideDriverDispatch;
import com.rideaustin.repo.dsl.RideDriverDispatchDslRepository;
import com.rideaustin.service.RequestedDriversRegistry;
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.utils.dispatch.StateMachineUtils;

public class AcceptanceGuardTest extends PersistingContextSupport {

  private static final long RIDE_ID = 1L;
  private static final long ACTIVE_DRIVER_ID = 2L;

  @Mock
  private RequestedDriversRegistry requestedDriversRegistry;
  @Mock
  private RideDriverDispatchDslRepository rideDriverDispatchDslRepository;

  @InjectMocks
  private AcceptanceGuard testedInstance;

  @Before
  public void setUp() throws Exception {
    super.setUp();

    testedInstance = new AcceptanceGuard();
    MockitoAnnotations.initMocks(this);

    DispatchCandidate candidate = new DispatchCandidate();
    candidate.setId(ACTIVE_DRIVER_ID);
    dispatchContext.setId(RIDE_ID);
    dispatchContext.setCandidate(candidate);
    StateMachineUtils.updateDispatchContext(context, dispatchContext, persister, environment);
  }

  @Test
  public void testEvaluateFalseWhenCandidateIsEmpty() {
    dispatchContext.setCandidate(null);
    StateMachineUtils.updateDispatchContext(context, dispatchContext, persister, environment);

    boolean result = testedInstance.evaluate(context);

    assertFalse(result);
  }

  @Test
  public void testEvaluateFalseWhenCandidateIsNotRequested() {
    when(requestedDriversRegistry.isRequested(ACTIVE_DRIVER_ID)).thenReturn(false);

    boolean result = testedInstance.evaluate(context);

    assertFalse(result);
  }

  @Test
  public void testEvaluateFalseWhenCandidateIsNotDispatched() {
    when(requestedDriversRegistry.isRequested(ACTIVE_DRIVER_ID)).thenReturn(true);
    when(rideDriverDispatchDslRepository.findByRideAndActiveDriverAndStatus(RIDE_ID, DispatchStatus.DISPATCHED, ACTIVE_DRIVER_ID))
      .thenReturn(null);

    boolean result = testedInstance.evaluate(context);

    assertFalse(result);
  }

  @Test
  public void testEvaluateTrueWhenCandidateRequestedAndDispatched() {
    when(requestedDriversRegistry.isRequested(ACTIVE_DRIVER_ID)).thenReturn(true);
    when(rideDriverDispatchDslRepository.findByRideAndActiveDriverAndStatus(RIDE_ID, DispatchStatus.DISPATCHED, ACTIVE_DRIVER_ID))
      .thenReturn(new RideDriverDispatch());

    boolean result = testedInstance.evaluate(context);

    assertTrue(result);
  }
}