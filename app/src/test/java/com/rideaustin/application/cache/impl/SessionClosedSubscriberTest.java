package com.rideaustin.application.cache.impl;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.service.SessionService.MessageSessionClosed;
import com.rideaustin.service.event.EventManager;

public class SessionClosedSubscriberTest {

  private SessionClosedSubscriber testedInstance;

  @Mock
  private EventManager eventManager;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new SessionClosedSubscriber(eventManager);
  }

  @Test
  public void handleMessage() {
    final long id = 1L;
    final MessageSessionClosed message = new MessageSessionClosed(id);

    testedInstance.handleMessage(message);

    verify(eventManager, times(1)).unregisterDriver(eq(id));
    verify(eventManager, times(1)).unregisterRider(eq(id));
  }
}