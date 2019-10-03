package com.rideaustin.service;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.events.AdminCancelledEvent;

public class RideFlowEventListenerTest {

  @Mock
  private RideFlowService rideFlowService;

  private RideFlowEventListener testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new RideFlowEventListener(rideFlowService);
  }

  @Test
  public void handleCallsAdminCancellation() {
    final long rideId = 1L;
    AdminCancelledEvent event = new AdminCancelledEvent(new Object(), rideId);

    testedInstance.handle(event);

    verify(rideFlowService, times(1)).cancelAsAdmin(eq(rideId));
  }
}