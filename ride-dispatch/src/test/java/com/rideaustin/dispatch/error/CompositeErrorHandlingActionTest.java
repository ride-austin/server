package com.rideaustin.dispatch.error;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.statemachine.action.Action;

import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;

public class CompositeErrorHandlingActionTest {

  @Mock
  private Action<States, Events> normalAction;
  @Mock
  private ErrorHandlingAction errorHandlingAction;

  private CompositeErrorHandlingAction testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new CompositeErrorHandlingAction(errorHandlingAction, normalAction);
  }

  @Test
  public void executeCallsErrorHandlingLast() {
    final InOrder order = inOrder(normalAction, errorHandlingAction);

    testedInstance.execute(null);

    order.verify(normalAction, times(1)).execute(any());
    order.verify(errorHandlingAction, times(1)).execute(any());
  }
}