package com.rideaustin.dispatch.guards;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.EnumSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.support.DefaultStateMachineContext;

import com.rideaustin.StubStateContext.StubExtendedState;
import com.rideaustin.application.cache.impl.JedisClient;
import com.rideaustin.dispatch.actions.PersistingContextSupport;
import com.rideaustin.service.config.RideAcceptanceConfig;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.utils.dispatch.StateMachineUtils;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

@RunWith(DataProviderRunner.class)
public class DispatchNotAcceptedGuardTest extends PersistingContextSupport {

  private static final Long RIDE_ID = 1L;

  @Mock
  private StateMachinePersist<States, Events, String> access;
  @Mock
  private JedisClient jedisClient;
  @Mock
  private RideAcceptanceConfig acceptanceConfig;

  @InjectMocks
  private DispatchNotAcceptedGuard testedInstance;

  @DataProvider
  public static Object[] ineligibleStates() {
    return EnumSet.complementOf(EnumSet.of(States.DISPATCH_PENDING, States.REQUESTED)).toArray();
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    testedInstance = new DispatchNotAcceptedGuard();
    MockitoAnnotations.initMocks(this);

    context.setSource(States.DISPATCH_PENDING);
    context.setTarget(States.REQUESTED);
    requestContext.setRideId(RIDE_ID);
    StateMachineUtils.updateRequestContext(context, requestContext, persister, environment);
  }

  @Test
  public void testEvaluateFalseWhenRideIsNotFound() {
    String machineId = StateMachineUtils.getMachineId(environment, RIDE_ID);
    when(jedisClient.exists(machineId)).thenReturn(false);

    boolean result = testedInstance.evaluate(context);

    assertFalse(result);
  }

  @Test
  public void testEvaluateFalseWhenPersistedContextNotFound() throws Exception {
    String machineId = StateMachineUtils.getMachineId(environment, RIDE_ID);
    when(jedisClient.exists(machineId)).thenReturn(true);
    when(access.read(machineId)).thenReturn(null);

    boolean result = testedInstance.evaluate(context);

    assertFalse(result);
  }

  @Test
  @UseDataProvider("ineligibleStates")
  public void testEvaluateFalseWhenWrongState(States state) throws Exception {
    String machineId = StateMachineUtils.getMachineId(environment, RIDE_ID);
    when(jedisClient.exists(machineId)).thenReturn(true);
    DefaultStateMachineContext<States, Events> stateMachineContext = new DefaultStateMachineContext<>(state, null, Collections.emptyMap(), new StubExtendedState());
    when(access.read(machineId)).thenReturn(stateMachineContext);

    boolean result = testedInstance.evaluate(context);

    assertFalse(result);
  }


  @Test
  public void testEvaluateFalseWhenAccepted() throws Exception {
    String machineId = StateMachineUtils.getMachineId(environment, RIDE_ID);
    when(jedisClient.exists(machineId)).thenReturn(true);
    DefaultStateMachineContext<States, Events> stateMachineContext = new DefaultStateMachineContext<>(States.DISPATCH_PENDING, null, Collections.emptyMap(), new StubExtendedState());
    dispatchContext.setAccepted(true);
    stateMachineContext.getExtendedState().getVariables().put("dispatchContext", dispatchContext);
    when(access.read(machineId)).thenReturn(stateMachineContext);

    boolean result = testedInstance.evaluate(context);

    assertFalse(result);
  }
}