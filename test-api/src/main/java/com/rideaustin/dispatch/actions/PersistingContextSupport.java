package com.rideaustin.dispatch.actions;

import org.junit.Before;
import org.mockito.Mock;
import org.springframework.core.env.Environment;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;

import com.rideaustin.StubStateContext;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.service.model.context.RideFlowContext;
import com.rideaustin.service.model.context.RideRequestContext;

public abstract class PersistingContextSupport {

  @Mock
  protected DefaultStateMachinePersister<States, Events, String> persister;
  @Mock
  protected Environment environment;

  protected RideFlowContext flowContext;
  protected RideRequestContext requestContext;
  protected DispatchContext dispatchContext;

  protected StubStateContext context;

  @Before
  public void setUp() throws Exception {
    this.context = new StubStateContext();
    flowContext = new RideFlowContext();
    requestContext = new RideRequestContext();
    dispatchContext = new DispatchContext();
  }
}
