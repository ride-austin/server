package com.rideaustin.dispatch.aop;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.statemachine.StateContext;
import org.springframework.web.context.request.async.DeferredResult;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.StubStateContext;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.RideRequestContext;

public class ActionAspectTest {

  @Mock
  private ProceedingJoinPoint joinPoint;
  @Mock
  private StateContext<States, Events> context;
  @Mock
  private DeferredResult<ResponseEntity<Object>> contextResult;

  private ActionAspect testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    testedInstance = new ActionAspect();

    when(joinPoint.getArgs()).thenReturn(new Object[]{
      context
    });
    when(context.getMessageHeader(eq("result"))).thenReturn(contextResult);
    when(context.getExtendedState())
      .thenReturn(new StubStateContext.StubExtendedState(ImmutableMap.of("requestContext", new RideRequestContext())));
  }

  @Test
  public void aspectSets200DeferredResultOnSuccess() throws Throwable {
    testedInstance.errorHandlingAspect(joinPoint);

    verify(contextResult, only()).setResult(eq(ResponseEntity.ok().build()));
  }

  @Test(expected = Exception.class)
  public void aspectSetsErrorResultOnFailure() throws Throwable {
    final Exception exception = new Exception();
    when(joinPoint.proceed()).thenThrow(exception);

    testedInstance.errorHandlingAspect(joinPoint);

    verify(contextResult, only()).setErrorResult(eq(exception));
  }
}