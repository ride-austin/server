package com.rideaustin.service.event;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.context.request.async.DeferredResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.rideaustin.assemblers.EventDtoAssembler;
import com.rideaustin.assemblers.MobileDriverRideDtoEnricher;
import com.rideaustin.model.Event;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.EventDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.model.EventDto;
import com.rideaustin.service.generic.TimeService;

@RunWith(MockitoJUnitRunner.class)
public class EventManagerTest {

  private static final long ID = 1L;
  private static final String DATE0 = "2015-12-31";
  private static final String DATE1 = "2016-01-01";
  private static final String DATE2 = "2016-01-15";
  private static final String DATE3 = "2016-01-31";
  private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private EventDslRepository eventRepository;

  @Mock
  private TimeService timeService;
  @Mock
  private EventDtoAssembler eventDtoAssembler;
  @Mock
  private ObjectMapper mapper;
  @Mock
  private RideDslRepository rideDslRepository;
  @Mock
  private MobileDriverRideDtoEnricher rideEnricher;

  private EventManager eventManager;

  private DeferredResult<List<EventDto>> deferredResult;

  @Before
  public void setupTests() throws ParseException {
    eventManager = new EventManager(eventRepository, timeService, eventDtoAssembler, rideDslRepository, rideEnricher, mapper);

    when(timeService.getCurrentDate()).thenReturn(DATE_FORMATTER.parse(DATE2));

    deferredResult = new DeferredResult<>(55000L, Collections.EMPTY_LIST);
  }

  @Test
  public void refreshEventsNoDrivers() {
    eventManager.refreshEvents();

    ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
    verify(eventRepository, times(1)).deleteInBatch(argument.capture());

    List arguments = argument.getAllValues();

    assertThat(((List) arguments.get(0)).isEmpty(), equalTo(true));
  }

  @Test
  public void refreshEventsOneDriverNoEvents() {
    User user = mockUser();
    eventManager.register(user.avatarInfo(AvatarType.DRIVER), user, null, deferredResult);
    eventManager.refreshEvents();

    ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
    verify(eventRepository, times(1)).deleteInBatch(argument.capture());

    List arguments = argument.getAllValues();

    assertThat(((List) arguments.get(0)).isEmpty(), equalTo(true));
  }

  @Test
  public void refreshEventsOneDriverWithEvents() throws Exception {
    User user = mockUser();
    when(eventRepository.listEvents(anySet(), Matchers.any())).thenReturn(mockEvents());

    eventManager.register(user.avatarInfo(AvatarType.DRIVER), user, null, deferredResult);
    eventManager.refreshEvents();

    ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
    verify(eventRepository, times(1)).deleteInBatch(argument.capture());

    List arguments = argument.getAllValues();

    assertThat(((List) arguments.get(0)).size(), equalTo(2));
    assertThat(((List<Event>) arguments.get(0)).get(0).getExpiresOn(), equalTo(DATE_FORMATTER.parse(DATE1)));
  }

  @Test
  public void refreshEventsOneDriverWithEventsFilterById() throws Exception {
    User user = mockUser();
    when(eventRepository.listEvents(anySet(), Matchers.any())).thenReturn(mockEvents());
    when(timeService.getCurrentDate()).thenReturn(DATE_FORMATTER.parse(DATE0));

    eventManager.register(user.avatarInfo(AvatarType.DRIVER), user, 0L, deferredResult);
    eventManager.refreshEvents();

    ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
    verify(eventRepository, times(1)).deleteInBatch(argument.capture());

    List arguments = argument.getAllValues();

    assertThat(((List) arguments.get(0)).size(), equalTo(0));

  }

  @Test
  public void refreshEventsOneDriverWithEventsFilterById2() throws Exception {
    User user = mockUser();
    when(eventRepository.listEvents(anySet(), Matchers.any())).thenReturn(mockEvents());
    when(timeService.getCurrentDate()).thenReturn(DATE_FORMATTER.parse(DATE0));

    eventManager.register(user.avatarInfo(AvatarType.DRIVER), user, 2L, deferredResult);
    eventManager.refreshEvents();

    ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
    verify(eventRepository, times(1)).deleteInBatch(argument.capture());

    List arguments = argument.getAllValues();

    assertThat(((List) arguments.get(0)).size(), equalTo(2));

  }

  private List mockEvents() throws ParseException {
    Event event1 = new Event();
    event1.setId(1L);
    event1.setAvatarId(ID);
    event1.setAvatarType(AvatarType.DRIVER);
    event1.setExpiresOn(DATE_FORMATTER.parse(DATE1));

    Event event2 = new Event();
    event2.setId(2L);
    event2.setAvatarId(ID);
    event2.setAvatarType(AvatarType.DRIVER);
    event2.setExpiresOn(DATE_FORMATTER.parse(DATE3));
    return Lists.newArrayList(event1, event2);
  }

  private User mockUser() {
    User user = new User();
    user.setId(ID);
    Driver avatar = new Driver();
    avatar.setId(ID);
    avatar.setActive(true);
    avatar.setUser(user);

    user.setAvatars(Lists.newArrayList(avatar));
    return user;
  }

}