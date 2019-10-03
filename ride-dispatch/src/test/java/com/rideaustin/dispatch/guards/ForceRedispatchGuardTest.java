package com.rideaustin.dispatch.guards;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.dispatch.actions.PersistingContextSupport;
import com.rideaustin.service.config.StackedRidesConfig;
import com.rideaustin.utils.dispatch.StateMachineUtils;

public class ForceRedispatchGuardTest extends PersistingContextSupport {

  private static final long CITY_ID = 1L;

  @Mock
  private StackedRidesConfig config;

  @InjectMocks
  private ForceRedispatchGuard testedInstance;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    testedInstance = new ForceRedispatchGuard();

    MockitoAnnotations.initMocks(this);
    requestContext.setCityId(CITY_ID);
    StateMachineUtils.updateRequestContext(context, requestContext, persister, environment);
  }

  @Test
  public void evaluateReturnsConfiguredValue() {
    final boolean expected = true;
    when(config.isForceRedispatchEnabled(eq(CITY_ID))).thenReturn(expected);

    final boolean result = testedInstance.evaluate(context);

    assertEquals(expected, result);
  }
}