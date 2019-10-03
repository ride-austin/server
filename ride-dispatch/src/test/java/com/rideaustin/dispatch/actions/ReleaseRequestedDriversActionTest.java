package com.rideaustin.dispatch.actions;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.StubStateContext;
import com.rideaustin.service.RequestedDriversRegistry;
import com.rideaustin.service.StackedDriverRegistry;
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.service.model.context.RideRequestContext;

public class ReleaseRequestedDriversActionTest {

  @Mock
  private StackedDriverRegistry stackedDriverRegistry;
  @Mock
  private RequestedDriversRegistry requestedDriversRegistry;

  @InjectMocks
  private ReleaseRequestedDriversAction testedInstance;

  @Before
  public void setUp() throws Exception {
    testedInstance = new ReleaseRequestedDriversAction();

    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void executeRemovesFromRequested() {
    final StubStateContext context = new StubStateContext();
    final DispatchContext dispatchContext = new DispatchContext();
    final DispatchCandidate candidate = new DispatchCandidate();
    final long candidateId = 1L;
    candidate.setId(candidateId);
    dispatchContext.setCandidate(candidate);
    context.getExtendedState().getVariables().putAll(
      ImmutableMap.of(
        "requestContext", new RideRequestContext(1L, 1L, 34.948981, -97.4891681, 1L,
          "REGULAR", 1, new ArrayList<>(), 10, null),
        "dispatchContext", dispatchContext
      )
    );
    when(requestedDriversRegistry.isRequested(eq(candidateId))).thenReturn(true);

    testedInstance.execute(context);

    verify(requestedDriversRegistry).remove(eq(candidateId));
  }

  @Test
  public void executeMarksStackable() {
    final StubStateContext context = new StubStateContext();
    final DispatchContext dispatchContext = new DispatchContext();
    final DispatchCandidate candidate = new DispatchCandidate();
    final long candidateId = 1L;
    candidate.setId(candidateId);
    dispatchContext.setCandidate(candidate);
    context.getExtendedState().getVariables().putAll(
      ImmutableMap.of(
        "requestContext", new RideRequestContext(1L, 1L, 34.948981, -97.4891681, 1L,
          "REGULAR", 1, new ArrayList<>(), 10, null),
        "dispatchContext", dispatchContext
      )
    );
    when(stackedDriverRegistry.isStacked(eq(candidateId))).thenReturn(true);

    testedInstance.execute(context);

    verify(stackedDriverRegistry).makeStackable(eq(candidateId));
  }

  @Test
  public void executeAddsCandidateToIgnored() {
    final StubStateContext context = new StubStateContext();
    final DispatchContext dispatchContext = new DispatchContext();
    final DispatchCandidate candidate = new DispatchCandidate();
    final long candidateId = 1L;
    candidate.setId(candidateId);
    dispatchContext.setCandidate(candidate);
    final RideRequestContext requestContext = new RideRequestContext(1L, 1L, 34.948981, -97.4891681, 1L,
      "REGULAR", 1, new ArrayList<>(), 10, null);
    context.getExtendedState().getVariables().putAll(
      ImmutableMap.of(
        "requestContext", requestContext,
        "dispatchContext", dispatchContext
      )
    );

    testedInstance.execute(context);

    assertEquals(1, requestContext.getIgnoreIds().size());
    assertEquals(candidateId, requestContext.getIgnoreIds().iterator().next().longValue());
  }

  @Test
  public void executeClearsDispatchContext() {
    final StubStateContext context = new StubStateContext();
    final DispatchContext dispatchContext = new DispatchContext();
    final DispatchCandidate candidate = new DispatchCandidate();
    final long candidateId = 1L;
    candidate.setId(candidateId);
    dispatchContext.setCandidate(candidate);
    final RideRequestContext requestContext = new RideRequestContext(1L, 1L, 34.948981, -97.4891681, 1L,
      "REGULAR", 1, new ArrayList<>(), 10, null);
    context.getExtendedState().getVariables().putAll(
      ImmutableMap.of(
        "requestContext", requestContext,
        "dispatchContext", dispatchContext
      )
    );

    testedInstance.execute(context);

    assertNull(dispatchContext.getCandidate());
    assertFalse(dispatchContext.isAccepted());
  }
}