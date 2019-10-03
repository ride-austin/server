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
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.utils.dispatch.StateMachineUtils;

public class DeclineGuardTest extends PersistingContextSupport {

  private static final long RIDE_ID = 1L;
  private static final long ACTIVE_DRIVER_ID = 2L;
  private static final long USER_ID = 3L;
  @Mock
  private RideDriverDispatchDslRepository rideDriverDispatchDslRepository;
  @InjectMocks
  private DeclineGuard testedInstance;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    testedInstance = new DeclineGuard();
    MockitoAnnotations.initMocks(this);

    requestContext.setRideId(RIDE_ID);
    dispatchContext.setId(RIDE_ID);
    DispatchCandidate candidate = new DispatchCandidate();
    candidate.setId(ACTIVE_DRIVER_ID);
    candidate.setUserId(USER_ID);
    dispatchContext.setCandidate(candidate);

    StateMachineUtils.updateRequestContext(context, requestContext, persister, environment);
    StateMachineUtils.updateDispatchContext(context, dispatchContext, persister, environment);
  }

  @Test
  public void testEvaluateFalseWhenDispatchNotFound() {
    when(environment.getProperty("dispatch.prepone_declined_driver_dispatches", Boolean.class, Boolean.TRUE)).thenReturn(true);
    when(rideDriverDispatchDslRepository.findByRideAndStatus(dispatchContext.getId(), DispatchStatus.DISPATCHED)).thenReturn(null);

    boolean result = testedInstance.evaluate(context);

    assertFalse(result);
  }

  @Test
  public void testEvaluateFalseWhenDispatchDoesntBelong() {
    when(environment.getProperty("dispatch.prepone_declined_driver_dispatches", Boolean.class, Boolean.TRUE)).thenReturn(true);
    when(rideDriverDispatchDslRepository.findByRideAndStatus(dispatchContext.getId(), DispatchStatus.DISPATCHED)).thenReturn(new RideDriverDispatch());
    context.addMessageHeader("userId", USER_ID+1);

    boolean result = testedInstance.evaluate(context);

    assertFalse(result);
  }

  @Test
  public void testEvaluateFalseWhenCandidateIsNotPresent() {
    when(environment.getProperty("dispatch.prepone_declined_driver_dispatches", Boolean.class, Boolean.TRUE)).thenReturn(true);
    when(rideDriverDispatchDslRepository.findByRideAndStatus(dispatchContext.getId(), DispatchStatus.DISPATCHED)).thenReturn(new RideDriverDispatch());
    dispatchContext.setCandidate(null);
    StateMachineUtils.updateDispatchContext(context, dispatchContext, persister, environment);

    boolean result = testedInstance.evaluate(context);

    assertFalse(result);
  }

  @Test
  public void testEvaluateFalseWhenNotConfigured() {
    when(environment.getProperty("dispatch.prepone_declined_driver_dispatches", Boolean.class, Boolean.TRUE)).thenReturn(false);
    when(rideDriverDispatchDslRepository.findByRideAndStatus(dispatchContext.getId(), DispatchStatus.DISPATCHED)).thenReturn(new RideDriverDispatch());
    context.addMessageHeader("userId", USER_ID);

    boolean result = testedInstance.evaluate(context);

    assertFalse(result);
  }

  @Test
  public void testEvaluateTrue() {
    when(environment.getProperty("dispatch.prepone_declined_driver_dispatches", Boolean.class, Boolean.TRUE)).thenReturn(true);
    when(rideDriverDispatchDslRepository.findByRideAndStatus(dispatchContext.getId(), DispatchStatus.DISPATCHED)).thenReturn(new RideDriverDispatch());
    context.addMessageHeader("userId", USER_ID);

    boolean result = testedInstance.evaluate(context);

    assertTrue(result);
  }
}