package com.rideaustin.dispatch.actions;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
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

public class DispatchNotAcceptedActionTest {

  @Mock
  private RequestedDriversRegistry requestedDriversRegistry;
  @Mock
  private StackedDriverRegistry stackedDriverRegistry;
  @Mock
  private RideDslRepository rideDslRepository;
  @Mock
  private RideDriverDispatchDslRepository rideDriverDispatchDslRepository;
  @Mock
  private ConsecutiveDeclineUpdateServiceFactory declineUpdateServiceFactory;
  @Mock
  private ConsecutiveDeclineUpdateService declineUpdateService;

  @InjectMocks
  private DispatchNotAcceptedAction testedInstance;

  @Before
  public void setUp() throws Exception {
    testedInstance = new DispatchNotAcceptedAction();
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void executeDeclinesRequestAsMissedRide() {
    final StubStateContext context = new StubStateContext();
    final DispatchContext dispatchContext = new DispatchContext();
    final DispatchCandidate candidate = new DispatchCandidate();
    final Ride ride = new Ride();
    final DispatchRequest dispatchRequest = new DispatchRequest(1L, 1L, 1L);
    dispatchContext.setId(1L);
    candidate.setId(1L);
    dispatchContext.setCandidate(candidate);
    dispatchContext.setDispatchType(DispatchType.REGULAR);
    context.getExtendedState().getVariables().put("dispatchContext", dispatchContext);
    context.getExtendedState().getVariables().put("requestContext", new RideRequestContext(1L, 1L, 34.654615, -97.464564, 1L, "REGULAR", 1, new ArrayList<>(),
      10, null));
    when(rideDslRepository.findOne(eq(dispatchContext.getId()))).thenReturn(ride);
    when(declineUpdateServiceFactory.createService(eq(DispatchType.REGULAR))).thenReturn(declineUpdateService);
    when(rideDriverDispatchDslRepository.findLastMissedRequestByRide(eq(dispatchContext.getId()))).thenReturn(dispatchRequest);

    final InOrder order = inOrder(rideDriverDispatchDslRepository, declineUpdateService);

    testedInstance.execute(context);

    order.verify(rideDriverDispatchDslRepository, times(1)).declineRequest(eq(dispatchContext.getId()), eq(candidate.getId()), eq(DispatchStatus.MISSED));
    order.verify(declineUpdateService, times(1)).processDriverDecline(eq(ride), eq(dispatchRequest));
  }
}