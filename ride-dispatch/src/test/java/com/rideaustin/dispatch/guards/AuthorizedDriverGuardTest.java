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
import com.rideaustin.service.ride.RideOwnerService;
import com.rideaustin.utils.dispatch.StateMachineUtils;

public class AuthorizedDriverGuardTest extends PersistingContextSupport {

  private static final long RIDE_ID = 1L;

  @Mock
  private RideOwnerService rideOwnerService;
  @InjectMocks
  private AuthorizedDriverGuard testedInstance;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    testedInstance = new AuthorizedDriverGuard();
    MockitoAnnotations.initMocks(this);

    requestContext.setRideId(RIDE_ID);
    StateMachineUtils.updateRequestContext(context, requestContext, persister, environment);
  }

  @Test
  public void testEvaluateFalseWhenRideDoesntBelong() {
    when(rideOwnerService.isDriversRide(RIDE_ID)).thenReturn(false);

    boolean result = testedInstance.evaluate(context);

    assertFalse(result);
  }

  @Test
  public void testEvaluateTrueWhenRideBelongs() {
    when(rideOwnerService.isDriversRide(RIDE_ID)).thenReturn(true);

    boolean result = testedInstance.evaluate(context);

    assertTrue(result);
  }
}