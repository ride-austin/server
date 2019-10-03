package com.rideaustin.dispatch.error;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.rideaustin.StubStateContext;

public class StopMachineActionTest {

  private StopMachineAction testedInstance;

  @Before
  public void setUp() throws Exception {
    testedInstance = new StopMachineAction();
  }

  @Test
  public void executeStopsMachine() {
    final StubStateContext context = new StubStateContext();
    context.getStateMachine().start();

    testedInstance.execute(context);

    assertTrue(((StubStateContext.StubStateMachine) context.getStateMachine()).isStopped());
  }
}