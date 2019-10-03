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
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.service.ride.RideOwnerService;

public class UpdateCommentGuardTest extends PersistingContextSupport {

  @Mock
  private RideOwnerService rideOwnerService;
  @Mock
  private RideDslRepository rideDslRepository;

  @InjectMocks
  private UpdateCommentGuard testedInstance;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    testedInstance = new UpdateCommentGuard();

    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void evaluateReturnsFalseWhenCommentDidntChange() {
    final long rideId = 1L;
    final long userId = 21L;
    final String comment = "Hello";
    final Ride ride = new Ride();
    ride.setComment(comment);
    context.getExtendedState().getVariables().put("rideId", rideId);
    context.addMessageHeader("comment", comment);
    context.addMessageHeader("userId", userId);
    when(rideDslRepository.findOne(rideId)).thenReturn(ride);
    when(rideOwnerService.isRideRider(userId, rideId)).thenReturn(true);

    final boolean result = testedInstance.evaluate(context);

    assertFalse(result);
  }

  @Test
  public void evaluateReturnsFalseWhenRideDoesntBelongToRider() {
    final long rideId = 1L;
    final long userId = 21L;
    final String comment = "Hello";
    final Ride ride = new Ride();
    context.getExtendedState().getVariables().put("rideId", rideId);
    context.addMessageHeader("comment", comment);
    context.addMessageHeader("userId", userId);
    when(rideDslRepository.findOne(rideId)).thenReturn(ride);
    when(rideOwnerService.isRideRider(userId, rideId)).thenReturn(false);

    final boolean result = testedInstance.evaluate(context);

    assertFalse(result);
  }

  @Test
  public void evaluateReturnsTrueWhenRiderOwnsRideAndCommentDiffers() {
    final long rideId = 1L;
    final long userId = 21L;
    final String comment = "Hello";
    final Ride ride = new Ride();
    context.getExtendedState().getVariables().put("rideId", rideId);
    context.addMessageHeader("comment", comment);
    context.addMessageHeader("userId", userId);
    when(rideDslRepository.findOne(rideId)).thenReturn(ride);
    when(rideOwnerService.isRideRider(userId, rideId)).thenReturn(true);

    final boolean result = testedInstance.evaluate(context);

    assertTrue(result);
  }
}