package com.rideaustin.dispatch;

import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.service.model.Events;

public class InceptionMachinesSubscriberTest {

  @Mock
  private InceptionMachinesTracker tracker;

  private InceptionMachinesSubscriber testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new InceptionMachinesSubscriber(tracker);
  }

  @Test
  public void handleKillInceptionMessage() {
    final long rideId = 1L;
    final KillInceptionMachineMessage message = new KillInceptionMachineMessage(rideId);

    testedInstance.handleMessage(message);

    verify(tracker, only()).stopMachines(eq(rideId));
  }

  @Test
  public void handleProxyMessage() {
    final long rideId = 1L;
    final Events event = Events.DISPATCH_REQUEST_ACCEPT;
    final String headerKey = "a";
    final String headerValue = "b";
    final MessageHeaders headers = new MessageHeaders(ImmutableMap.of(headerKey, headerValue));
    final ProxyEventMessage message = new ProxyEventMessage(rideId, event, headers);

    testedInstance.handleMessage(message);

    verify(tracker, only()).proxyEvent(eq(rideId), argThat(new BaseMatcher<GenericMessage<Events>>() {
      @Override
      public boolean matches(Object o) {
        final GenericMessage<Events> message = (GenericMessage<Events>) o;
        return message.getPayload() == event
          && message.getHeaders().containsKey(headerKey)
          && message.getHeaders().get(headerKey).equals(headerValue);
      }

      @Override
      public void describeTo(Description description) {

      }
    }));
  }
}