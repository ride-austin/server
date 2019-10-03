package com.rideaustin.dispatch.actions;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.StubStateContext;
import com.rideaustin.dispatch.service.ConsecutiveDeclineUpdateService;
import com.rideaustin.dispatch.service.ConsecutiveDeclineUpdateServiceFactory;
import com.rideaustin.model.enums.DispatchStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.dsl.RideDriverDispatchDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.service.RequestedDriversRegistry;
import com.rideaustin.service.StackedDriverRegistry;
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.service.model.DispatchRequest;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.service.model.context.DispatchType;
import com.rideaustin.service.model.context.RideRequestContext;

public class DispatchDeclineActionTest {

  @Mock
  private RequestedDriversRegistry requestedDriversRegistry;
  @Mock
  private StackedDriverRegistry stackedDriverRegistry;
  @Mock
  private RideDslRepository rideDslRepository;
  @Mock
  private RideDriverDispatchDslRepository rideDriverDispatchDslRepository;
  @Mock
  private ConsecutiveDeclineUpdateServiceFactory consecutiveDeclineUpdateServiceFactory;
  @Mock
  private ConsecutiveDeclineUpdateService consecutiveDeclineUpdateService;

  @InjectMocks
  private DispatchDeclineAction testedInstance;

  @Before
  public void setUp() throws Exception {
    testedInstance = new DispatchDeclineAction();

    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void executeProcessesAllConsecutiveDeclines() {
    final DispatchRequest dispatchRequest = new DispatchRequest(1L, 1L, 1L);
    final Ride ride = new Ride();
    final StubStateContext context = prepareStateContext(dispatchRequest, ride);

    testedInstance.execute(context);

    verify(consecutiveDeclineUpdateService, times(1)).processDriverDecline(eq(ride), eq(dispatchRequest));
  }

  @Test
  public void executeDeclinesRequest() {
    final DispatchRequest dispatchRequest = new DispatchRequest(1L, 1L, 1L);
    final Ride ride = new Ride();
    final StubStateContext context = prepareStateContext(dispatchRequest, ride);

    testedInstance.execute(context);

    final DispatchContext dispatchContext = context.getExtendedState().get("dispatchContext", DispatchContext.class);

    verify(rideDriverDispatchDslRepository, times(1)).declineRequest(anyLong(), anyLong(), eq(DispatchStatus.DECLINED));
  }

  private StubStateContext prepareStateContext(DispatchRequest dispatchRequest, Ride ride) {
    when(rideDslRepository.findOne(anyLong())).thenReturn(ride);
    when(rideDriverDispatchDslRepository.findDispatchedRequests(any(Ride.class))).thenReturn(Collections.singletonList(dispatchRequest));
    when(consecutiveDeclineUpdateServiceFactory.createService(any())).thenReturn(consecutiveDeclineUpdateService);

    final StubStateContext context = new StubStateContext();
    final DispatchContext dispatchContext = new DispatchContext(1L, 1L, 34.654615, -97.464564, DispatchType.REGULAR);
    final DispatchCandidate candidate = new DispatchCandidate();
    final RideRequestContext requestContext = new RideRequestContext(1L, 1L, 34.654615, -97.464564, 1L, "REGULAR", 1, new ArrayList<>(),
      10, null);
    candidate.setId(1L);
    dispatchContext.setCandidate(candidate);
    context.getExtendedState().getVariables().put("rideId", 1L);
    context.getExtendedState().getVariables().put("dispatchContext", dispatchContext);
    context.getExtendedState().getVariables().put("requestContext", requestContext);
    return context;
  }
}