package com.rideaustin.dispatch;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.HashSet;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.statemachine.StateMachine;

import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;

public class InceptionMachinesTrackerTest {

  @Mock
  private StateMachine<States, Events> machine;

  private InceptionMachinesTracker testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new InceptionMachinesTracker();
  }

  @Test
  public void startTrackingAddsNewRideMachine() {
    testedInstance.startTracking(1L, machine);

    assertTrue(testedInstance.getMachines().containsKey(1L));
    assertTrue(CollectionUtils.isEqualCollection(testedInstance.getMachines().get(1L), Collections.singleton(machine)));
  }

  @Test
  public void stopTrackingStopsMachineAndRemovesIt() {
    final long rideId = 1L;
    testedInstance.getMachines().put(rideId, new HashSet<>(Collections.singleton(machine)));

    testedInstance.stopMachines(rideId);

    verify(machine, times(1)).stop();
    assertFalse(testedInstance.getMachines().containsKey(rideId));
  }

  @Test
  public void proxyEventSendsEventToMachines() {
    final long rideId = 1L;
    testedInstance.getMachines().put(rideId, new HashSet<>(Collections.singleton(machine)));

    final GenericMessage<Events> message = new GenericMessage<>(Events.DISPATCH_REQUEST_ACCEPT, new MessageHeaders(Collections.emptyMap()));
    testedInstance.proxyEvent(rideId, message);

    verify(machine, times(1)).sendEvent(eq(message));
  }

}