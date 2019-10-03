package com.rideaustin.dispatch.guards;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.EnumSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.dispatch.actions.PersistingContextSupport;
import com.rideaustin.filter.ClientType;
import com.rideaustin.service.model.States;
import com.rideaustin.utils.dispatch.StateMachineUtils;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

@RunWith(DataProviderRunner.class)
public class RedispatchOnCancelGuardTest extends PersistingContextSupport {

  @Mock
  private ConfigurationItemCache configurationItemCache;

  @InjectMocks
  private RedispatchOnCancelGuard testedInstance;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    testedInstance = new RedispatchOnCancelGuard();

    MockitoAnnotations.initMocks(this);
  }

  @DataProvider
  public static Object[] ineligibleStates() {
    return EnumSet.complementOf(EnumSet.of(States.DRIVER_ASSIGNED)).toArray();
  }

  @Test
  @UseDataProvider("ineligibleStates")
  public void evaluateReturnsFalseWhenRideIsNotAssigned(States state) {
    context.setSource(state);

    final boolean result = testedInstance.evaluate(context);

    assertFalse(result);
  }

  @Test
  public void evaluateReturnsConfiguredForDriverAssigned() {
    context.setSource(States.DRIVER_ASSIGNED);
    when(configurationItemCache.getConfigAsBoolean(eq(ClientType.CONSOLE), anyString(),
      eq("enabled"), anyLong())).thenReturn(true);
    dispatchContext.setCityId(1L);

    StateMachineUtils.updateDispatchContext(context, dispatchContext, persister, environment);

    final boolean result = testedInstance.evaluate(context);

    assertTrue(result);
  }
}