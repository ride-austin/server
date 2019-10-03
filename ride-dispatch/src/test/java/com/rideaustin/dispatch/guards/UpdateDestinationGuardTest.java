package com.rideaustin.dispatch.guards;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.dispatch.actions.PersistingContextSupport;
import com.rideaustin.rest.model.RideEndLocation;
import com.rideaustin.service.ride.RideOwnerService;

public class UpdateDestinationGuardTest extends PersistingContextSupport {

  @Mock
  private RideOwnerService rideOwnerService;

  @InjectMocks
  private UpdateDestinationGuard testedInstance;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    testedInstance = new UpdateDestinationGuard();

    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void evaluateReturnsFalseWhenRideDoesntBelongToRider() {
    final long rideId = 1L;
    final long userId = 12L;
    final RideEndLocation location = new RideEndLocation();
    location.setEndLocationLat(34.6846186);
    location.setEndLocationLong(-97.86161);
    context.addMessageHeader("endLocation", location);
    context.addMessageHeader("userId", userId);
    context.getExtendedState().getVariables().put("rideId", rideId);
    when(rideOwnerService.isRideRider(userId, rideId)).thenReturn(false);

    final boolean result = testedInstance.evaluate(context);

    assertFalse(result);
  }

  @Test
  public void evaluateReturnsFalseWhenLocationIsEmpty() {
    final long rideId = 1L;
    final long userId = 12L;
    final RideEndLocation location = new RideEndLocation();
    context.addMessageHeader("endLocation", location);
    context.addMessageHeader("userId", userId);
    context.getExtendedState().getVariables().put("rideId", rideId);
    when(rideOwnerService.isRideRider(userId, rideId)).thenReturn(true);

    final boolean result = testedInstance.evaluate(context);

    assertFalse(result);
  }

  @Test
  public void evaluateReturnsTrueWhenLocationIsSetAndRiderOwnsRide() {
    final long rideId = 1L;
    final long userId = 12L;
    final RideEndLocation location = new RideEndLocation();
    location.setEndLocationLat(34.6846186);
    location.setEndLocationLong(-97.86161);
    context.addMessageHeader("endLocation", location);
    context.addMessageHeader("userId", userId);
    context.getExtendedState().getVariables().put("rideId", rideId);
    when(rideOwnerService.isRideRider(userId, rideId)).thenReturn(true);

    final boolean result = testedInstance.evaluate(context);

    assertTrue(result);
  }
}