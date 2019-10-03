package com.rideaustin.assemblers;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.statemachine.StateMachinePersist;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.StubStateMachineContext;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.rest.model.RideHistoryDto;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.RideFlowContext;

public class RideHistoryDtoEnricherTest {

  @Mock
  private StateMachinePersist<States, Events, String> contextAccess;
  @Mock
  private Environment environment;

  private RideHistoryDtoEnricher testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new RideHistoryDtoEnricher(contextAccess, environment);
  }

  @Test
  public void enrichSkipsNull() {
    final RideHistoryDto result = testedInstance.enrich(null);

    assertNull(result);
  }

  @Test
  public void enrichSetsStartedOn() throws Exception {
    RideHistoryDto source = new RideHistoryDto(1L, RideStatus.ACTIVE, "A", "B", "C",
      "D", null, new Date(), new Date(), null, null, null, null);

    final RideFlowContext rideFlowContext = new RideFlowContext();
    final Date startedOn = new Date();
    rideFlowContext.setStartedOn(startedOn);
    when(contextAccess.read(anyString())).thenReturn(new StubStateMachineContext(ImmutableMap.of(
      "flowContext", rideFlowContext
    )));

    final RideHistoryDto result = testedInstance.enrich(source);

    assertEquals(startedOn, result.getStartedOn());
  }
}