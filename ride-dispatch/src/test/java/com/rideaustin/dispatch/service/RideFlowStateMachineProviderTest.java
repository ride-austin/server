package com.rideaustin.dispatch.service;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachinePersist;

import com.google.maps.model.LatLng;
import com.rideaustin.StubStateContext;
import com.rideaustin.StubStateContext.StubExtendedState;
import com.rideaustin.StubStateMachineContext;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.service.areaqueue.AreaService;
import com.rideaustin.service.config.RideDispatchServiceConfig;
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.RideRequestContext;

public class RideFlowStateMachineProviderTest {

  @Mock
  private Environment environment;
  @Mock
  private BeanFactory beanFactory;
  @Mock
  private RideDispatchServiceConfig config;
  @Mock
  private AreaService areaService;
  @Mock
  private RideDslRepository rideDslRepository;
  @Mock
  private StateMachinePersist<States, Events, String> persister;
  @Mock
  private RedisTemplate<byte[], byte[]> redisTemplate;
  @Mock
  private ChannelTopic inceptionMachineTopic;

  private RideFlowStateMachineProvider testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new RideFlowStateMachineProvider(environment, beanFactory, config, areaService, rideDslRepository,
      persister, redisTemplate, inceptionMachineTopic);
  }

  @Test
  public void createMachineCreatesInceptionMachine() {
    final RideRequestContext requestContext = new RideRequestContext();
    final StubExtendedState extendedState = new StubExtendedState();
    when(beanFactory.getBean(anyString())).thenReturn(new StubStateContext.StubStateMachine(extendedState, null));

    testedInstance.createMachine(requestContext);

    assertEquals(requestContext, extendedState.getVariables().get("requestContext"));
    assertTrue((boolean) extendedState.getVariables().get("_inception"));
  }

  @Test
  public void restoreMachineReturnsEmptyWhenContextAndRideNotFound() throws Exception {
    when(beanFactory.getBean(anyString())).thenReturn(new StubStateContext.StubStateMachine(new StubExtendedState(), null));
    when(persister.read(anyString())).thenReturn(null);

    final Optional<StateMachine<States, Events>> result = testedInstance.restoreMachine(1L, Events.DISPATCH_REQUEST_ACCEPT, new MessageHeaders(Collections.emptyMap()));

    assertFalse(result.isPresent());
  }

  @Test
  public void restoreMachineForcesRestoreFromDbWhenContextNotFound() throws Exception {
    when(beanFactory.getBean(anyString())).thenReturn(new StubStateContext.StubStateMachine(new StubExtendedState(), null));
    when(persister.read(anyString())).thenReturn(null);
    final Ride ride = new Ride();
    ride.setStartLocationLat(34.61861);
    ride.setStartLocationLong(-97.47916);
    ride.setCityId(1L);
    ride.setActiveDriver(new ActiveDriver());
    when(rideDslRepository.findOne(anyLong())).thenReturn(ride);
    when(areaService.isInArea(any(LatLng.class), anyLong())).thenReturn(null);
    when(rideDslRepository.findDispatchCandidate(anyLong())).thenReturn(new DispatchCandidate());

    final Optional<StateMachine<States, Events>> result = testedInstance.restoreMachine(1L, Events.DISPATCH_REQUEST_ACCEPT, new MessageHeaders(Collections.emptyMap()));

    assertTrue(result.isPresent());

  }

  @Test
  public void restoreMachineGetsContext() throws Exception {
    when(beanFactory.getBean(anyString())).thenReturn(new StubStateContext.StubStateMachine(new StubExtendedState(), null));
    when(persister.read(anyString())).thenReturn(new StubStateMachineContext(new HashMap<>()));

    final Optional<StateMachine<States, Events>> result = testedInstance.restoreMachine(1L, Events.DISPATCH_REQUEST_ACCEPT, new MessageHeaders(Collections.emptyMap()));

    assertTrue(result.isPresent());
  }
}