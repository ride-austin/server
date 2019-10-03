package com.rideaustin.dispatch.service;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.persist.StateMachinePersister;

import com.rideaustin.StubStateContext;
import com.rideaustin.dispatch.InceptionMachinesTracker;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;

public class RideFlowStateMachineListenerTest {

  @Mock
  private StateMachinePersister<States, Events, String> persister;
  @Mock
  private StateMachinePersist<States, Events, String> contextAccess;
  @Mock
  private Environment environment;
  @Mock
  private InceptionMachinesTracker machineTracker;

  @InjectMocks
  private RideFlowStateMachineListener testedInstance;

  @Before
  public void setUp() throws Exception {
    testedInstance = new RideFlowStateMachineListener();
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void stateContextChangeDoesntPersistWhenRideIsMissing() throws Exception {
    final StubStateContext stateContext = new StubStateContext();

    testedInstance.stateContext(stateContext);

    verify(contextAccess, never()).read(anyString());
  }

  @Test
  public void stateContextChangeDoesntPersistWhenStateIsMissing() throws Exception {
    final StubStateContext stateContext = new StubStateContext();
    stateContext.getExtendedState().getVariables().put("rideId", 1L);

    testedInstance.stateContext(stateContext);

    verify(contextAccess, never()).read(anyString());
  }

}