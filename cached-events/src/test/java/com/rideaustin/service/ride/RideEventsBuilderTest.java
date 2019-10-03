package com.rideaustin.service.ride;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.Environment;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.model.RideEvents;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

@RunWith(DataProviderRunner.class)
public class RideEventsBuilderTest {

  private static final int THRESHOLD = 86400;
  @Mock
  private RideDslRepository rideDslRepository;
  @Mock
  private BeanFactory beanFactory;
  @Mock
  private Environment environment;

  private RideEventsBuilder testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    when(environment.getProperty(anyString(), eq(Integer.class), anyInt())).thenReturn(THRESHOLD);

    testedInstance = new RideEventsBuilder(rideDslRepository, beanFactory, environment);
  }

  @DataProvider
  public static Object[] terminalStatuses() {
    return RideStatus.TERMINAL_STATUSES.toArray();
  }

  @Test
  @UseDataProvider("terminalStatuses")
  public void testBuildEventsSkipsEventForRideInTerminalStatus(RideStatus status) throws Exception {
    RideEvents events = setupEvent(new Date());

    when(rideDslRepository.getStatuses(Collections.singleton(1L))).thenReturn(ImmutableMap.of(1L, status));

    List<RideEvent> result = testedInstance.buildEvents(events);

    assertTrue(result.isEmpty());
  }

  @Test
  public void testBuildEventsSkipsEventsFromFuture() {
    RideEvents events = setupEvent(Date.from(Instant.now().plus(1, ChronoUnit.MINUTES)));

    when(rideDslRepository.getStatuses(Collections.singleton(1L))).thenReturn(ImmutableMap.of(1L, RideStatus.ACTIVE));

    List<RideEvent> result = testedInstance.buildEvents(events);

    assertTrue(result.isEmpty());
  }

  @Test
  public void testBuildEventsSkipsEventsFromPast() {
    RideEvents events = setupEvent(Date.from(Instant.now().minus(THRESHOLD+1, ChronoUnit.SECONDS)));

    when(rideDslRepository.getStatuses(Collections.singleton(1L))).thenReturn(ImmutableMap.of(1L, RideStatus.ACTIVE));

    List<RideEvent> result = testedInstance.buildEvents(events);

    assertTrue(result.isEmpty());
  }

  @Test
  public void testBuildEventsAddsValidEvent() {
    RideEvents events = setupEvent(new Date());

    when(rideDslRepository.getStatuses(Collections.singleton(1L))).thenReturn(ImmutableMap.of(1L, RideStatus.ACTIVE));

    List<RideEvent> result = testedInstance.buildEvents(events);

    assertEquals(1, result.size());
  }

  private RideEvents setupEvent(Date date) {
    RideEvents events = new RideEvents();
    ImmutableMap<String, String> map = ImmutableMap.of("rideId", "1", "eventTimestamp", String.valueOf(date.getTime()), "eventType", "START_RIDE");
    events.setEvents(Collections.singletonList(map));
    when(beanFactory.getBean(any(Class.class), any(Object[].class))).thenReturn(new StubEvent(map));
    return events;
  }

  static class StubEvent extends RideEvent {

    StubEvent(Map<String, String> eventProperties) {
      super(eventProperties);
    }
  }

}