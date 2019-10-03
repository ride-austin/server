package com.rideaustin.dispatch.guards;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.rideaustin.dispatch.actions.PersistingContextSupport;
import com.rideaustin.rest.model.RideEndLocation;
import com.rideaustin.utils.dispatch.StateMachineUtils;

public class EndRideGuardTest extends PersistingContextSupport {

  private EndRideGuard testedInstance;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    testedInstance = new EndRideGuard();
  }

  @Test
  public void evaluateToFalseWhenCompletedOnIsEmpty() {
    final RideEndLocation endLocation = createEndLocation();
    context.addMessageHeader("endLocation", endLocation);
    context.addMessageHeader("completedOn", null);

    final boolean result = testedInstance.evaluate(context);

    assertFalse(result);
  }

  @Test
  public void evaluateToFalseWhenStartedOnIsEmpty() {
    final RideEndLocation endLocation = createEndLocation();
    context.addMessageHeader("endLocation", endLocation);
    context.addMessageHeader("completedOn", new Date());
    flowContext.setStartedOn(null);
    StateMachineUtils.updateFlowContext(context, flowContext, persister, environment);

    final boolean result = testedInstance.evaluate(context);

    assertFalse(result);
  }

  @Test
  public void evaluateToFalseWhenStartedAfterCompleted() {
    final RideEndLocation endLocation = createEndLocation();
    final Date completedOn = new Date();
    context.addMessageHeader("endLocation", endLocation);
    context.addMessageHeader("completedOn", completedOn);
    flowContext.setStartedOn(Date.from(Instant.now().plus(10, ChronoUnit.SECONDS)));
    StateMachineUtils.updateFlowContext(context, flowContext, persister, environment);

    final boolean result = testedInstance.evaluate(context);

    assertFalse(result);
  }

  @Test
  public void evaluateToTrueWhenStartedBeforeCompleted() {
    final RideEndLocation endLocation = createEndLocation();
    final Date completedOn = new Date();
    context.addMessageHeader("endLocation", endLocation);
    context.addMessageHeader("completedOn", completedOn);
    flowContext.setStartedOn(Date.from(Instant.now().minus(10, ChronoUnit.SECONDS)));
    StateMachineUtils.updateFlowContext(context, flowContext, persister, environment);

    final boolean result = testedInstance.evaluate(context);

    assertTrue(result);
  }

  private RideEndLocation createEndLocation() {
    final RideEndLocation endLocation = new RideEndLocation();
    endLocation.setEndLocationLat(34.8946);
    endLocation.setEndLocationLong(-97.48616);
    return endLocation;
  }


}