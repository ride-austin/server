package com.rideaustin.dispatch.actions;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Date;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import com.rideaustin.StubStateContext;
import com.rideaustin.events.DriverReachedEvent;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.service.model.context.DispatchType;
import com.rideaustin.service.model.context.RideFlowContext;
import com.rideaustin.service.model.context.RideRequestContext;
import com.rideaustin.service.notification.RideFlowPushNotificationFacade;

public class DriverReachActionTest {

  @Mock
  private RideFlowPushNotificationFacade pushNotificationsFacade;
  @Mock
  private ApplicationEventPublisher publisher;
  @Mock
  private RideDslRepository rideDslRepository;

  @InjectMocks
  private DriverReachAction testedInstance;

  @Before
  public void setUp() throws Exception {
    testedInstance = new DriverReachAction();
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void executeSetsReachedOn() {
    final RideFlowContext flowContext = new RideFlowContext();
    final Date reachedDate = new Date();

    final StubStateContext context = setupContext(flowContext, reachedDate);

    testedInstance.execute(context);

    assertEquals(reachedDate, flowContext.getReachedOn());
  }

  @Test
  public void executeNotifiesRider() {
    final RideFlowContext flowContext = new RideFlowContext();
    final Date reachedDate = new Date();

    final StubStateContext context = setupContext(flowContext, reachedDate);

    testedInstance.execute(context);

    verify(rideDslRepository, times(1)).setStatus(eq(1L), eq(RideStatus.DRIVER_REACHED));
    verify(pushNotificationsFacade, times(1)).sendRideUpdateToRider(eq(1L));
    verify(publisher, times(1)).publishEvent(argThat(new BaseMatcher<DriverReachedEvent>() {
      @Override
      public boolean matches(Object o) {
        return ((DriverReachedEvent) o).getRideId() == 1L;
      }

      @Override
      public void describeTo(Description description) {

      }
    }));

  }

  private StubStateContext setupContext(RideFlowContext flowContext, Date reachedDate) {
    final StubStateContext context = new StubStateContext();
    final DispatchContext dispatchContext = new DispatchContext();
    final DispatchCandidate candidate = new DispatchCandidate();
    dispatchContext.setId(1L);
    candidate.setId(1L);
    dispatchContext.setCandidate(candidate);
    dispatchContext.setDispatchType(DispatchType.REGULAR);
    context.getExtendedState().getVariables().put("dispatchContext", dispatchContext);
    context.getExtendedState().getVariables().put("requestContext", new RideRequestContext(1L, 1L, 34.654615, -97.464564, 1L, "REGULAR", 1, new ArrayList<>(),
      10, null));
    context.getExtendedState().getVariables().put("flowContext", flowContext);
    context.addMessageHeader("reachedDate", reachedDate);
    return context;
  }
}