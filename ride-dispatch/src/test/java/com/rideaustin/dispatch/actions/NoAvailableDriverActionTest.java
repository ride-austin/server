package com.rideaustin.dispatch.actions;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.StubStateContext;
import com.rideaustin.dispatch.KillInceptionMachineMessage;
import com.rideaustin.dispatch.service.ConsecutiveDeclineUpdateService;
import com.rideaustin.dispatch.service.ConsecutiveDeclineUpdateServiceFactory;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.dsl.ActiveDriverDslRepository;
import com.rideaustin.repo.dsl.RideDriverDispatchDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.service.model.DispatchRequest;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.service.model.context.DispatchType;
import com.rideaustin.service.notification.RideFlowPushNotificationFacade;
import com.rideaustin.service.thirdparty.StripeService;

public class NoAvailableDriverActionTest {

  @Mock
  private RideDslRepository rideDslRepository;
  @Mock
  private RideDriverDispatchDslRepository rideDriverDispatchDslRepository;
  @Mock
  private ActiveDriverDslRepository activeDriverDslRepository;
  @Mock
  private RideFlowPushNotificationFacade pushNotificationsFacade;
  @Mock
  private StripeService stripeService;
  @Mock
  private ConsecutiveDeclineUpdateServiceFactory consecutiveDeclineUpdateServiceFactory;
  @Mock
  private ChannelTopic inceptionMachinesTopic;
  @Mock
  private RedisTemplate redisTemplate;
  @Mock
  private ConsecutiveDeclineUpdateService consecutiveDeclineUpdateService;

  @InjectMocks
  private NoAvailableDriverAction testedInstance;

  @Before
  public void setUp() throws Exception {
    testedInstance = new NoAvailableDriverAction();

    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void executeAbortsOnAbsentRideId() {
    final StubStateContext context = new StubStateContext();

    testedInstance.execute(context);

    verify(rideDslRepository, never()).findOne(anyLong());
  }

  @Test
  public void executeMarksDispatchRequestsAsDeclined() {
    final StubStateContext context = new StubStateContext();
    final DispatchContext dispatchContext = new DispatchContext();
    final long rideId = 1L;
    dispatchContext.setDispatchType(DispatchType.REGULAR);
    context.getExtendedState().getVariables().putAll(ImmutableMap.of(
      "dispatchContext", dispatchContext,
      "rideId", rideId
    ));
    final Ride ride = new Ride();
    final DispatchRequest dispatchRequest = new DispatchRequest(1L, 1L, 1L);
    when(rideDslRepository.findOne(eq(rideId))).thenReturn(ride);
    when(rideDriverDispatchDslRepository.findDispatchedRequests(eq(ride))).thenReturn(Collections.singletonList(dispatchRequest));
    when(consecutiveDeclineUpdateServiceFactory.createService(eq(dispatchContext.getDispatchType()))).thenReturn(consecutiveDeclineUpdateService);

    testedInstance.execute(context);

    verify(consecutiveDeclineUpdateService).processDriverDecline(eq(ride), eq(dispatchRequest));
    verify(rideDriverDispatchDslRepository).missRequests(anyListOf(Long.class));
  }

  @Test
  public void executeRefundsPrecharge() throws RideAustinException {
    final StubStateContext context = new StubStateContext();
    final DispatchContext dispatchContext = new DispatchContext();
    final long rideId = 1L;
    dispatchContext.setDispatchType(DispatchType.REGULAR);
    context.getExtendedState().getVariables().putAll(ImmutableMap.of(
      "dispatchContext", dispatchContext,
      "rideId", rideId
    ));
    final Ride ride = new Ride();
    when(rideDslRepository.findOne(eq(rideId))).thenReturn(ride);

    testedInstance.execute(context);

    verify(stripeService).refundPreCharge(eq(ride));
  }

  @Test
  public void executeUpdatesRideStatus() {
    final StubStateContext context = new StubStateContext();
    final DispatchContext dispatchContext = new DispatchContext();
    final long rideId = 1L;
    dispatchContext.setDispatchType(DispatchType.REGULAR);
    context.getExtendedState().getVariables().putAll(ImmutableMap.of(
      "dispatchContext", dispatchContext,
      "rideId", rideId
    ));
    final Ride ride = new Ride();
    when(rideDslRepository.findOne(eq(rideId))).thenReturn(ride);

    testedInstance.execute(context);

    assertEquals(RideStatus.NO_AVAILABLE_DRIVER, ride.getStatus());
  }

  @Test
  public void executeNotifiesRider() {
    final StubStateContext context = new StubStateContext();
    final DispatchContext dispatchContext = new DispatchContext();
    final long rideId = 1L;
    dispatchContext.setDispatchType(DispatchType.REGULAR);
    context.getExtendedState().getVariables().putAll(ImmutableMap.of(
      "dispatchContext", dispatchContext,
      "rideId", rideId
    ));
    final Ride ride = new Ride();
    when(rideDslRepository.findOne(eq(rideId))).thenReturn(ride);

    testedInstance.execute(context);

    verify(pushNotificationsFacade).sendRideUpdateToRider(eq(rideId));
  }

  @Test
  public void executeSetsAssignedDriverAsAvailable() {
    final StubStateContext context = new StubStateContext();
    final DispatchContext dispatchContext = new DispatchContext();
    final long rideId = 1L;
    final long candidateId = 1L;
    final DispatchCandidate candidate = new DispatchCandidate();
    candidate.setId(candidateId);
    dispatchContext.setDispatchType(DispatchType.REGULAR);
    dispatchContext.setCandidate(candidate);
    context.getExtendedState().getVariables().putAll(ImmutableMap.of(
      "dispatchContext", dispatchContext,
      "rideId", rideId
    ));
    final Ride ride = new Ride();
    when(rideDslRepository.findOne(eq(rideId))).thenReturn(ride);

    testedInstance.execute(context);

    verify(activeDriverDslRepository).setRidingDriverAsAvailable(eq(candidateId));
  }

  @Test
  public void executePublishesKillInceptionMessage() {
    final StubStateContext context = new StubStateContext();
    final DispatchContext dispatchContext = new DispatchContext();
    final long rideId = 1L;
    context.getExtendedState().getVariables().putAll(ImmutableMap.of(
      "dispatchContext", dispatchContext,
      "rideId", rideId
    ));
    final Ride ride = new Ride();
    when(rideDslRepository.findOne(eq(rideId))).thenReturn(ride);

    testedInstance.execute(context);

    verify(redisTemplate).convertAndSend(anyString(), argThat(new BaseMatcher<KillInceptionMachineMessage>() {
      @Override
      public boolean matches(Object o) {
        return ((KillInceptionMachineMessage) o).getRideId().equals(rideId);
      }

      @Override
      public void describeTo(Description description) {

      }
    }));
  }
}