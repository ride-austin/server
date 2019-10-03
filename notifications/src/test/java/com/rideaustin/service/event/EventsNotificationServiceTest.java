package com.rideaustin.service.event;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Objects;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.model.Event;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.EventType;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.jpa.EventRepository;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.model.DispatchCandidate;

public class EventsNotificationServiceTest {

  @Mock
  private EventRepository eventRepository;
  @Spy
  private ObjectMapper objectMapper;

  private EventsNotificationService testedInstance;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new EventsNotificationService(eventRepository, objectMapper);
  }

  @Test
  public void sendRideRequestSavesEvent() {
    final Ride ride = new Ride();
    final DispatchCandidate candidate = new DispatchCandidate();
    candidate.setDriverId(1L);
    testedInstance.sendRideRequest(ride, candidate, 10L, Date.from(Instant.now().plus(10, ChronoUnit.SECONDS)),
      Date.from(Instant.now().plus(20, ChronoUnit.SECONDS)), 60L);

    verify(eventRepository).save(argThat(new EventMatcher(ride, candidate.getDriverId(), AvatarType.DRIVER, EventType.REQUESTED)));
  }

  @Test
  public void sendHandshakeRequestSavesEvent() {
    final DispatchCandidate candidate = new DispatchCandidate();
    candidate.setDriverId(1L);

    testedInstance.sendHandshakeRequest(1L, candidate, 10, new Date());

    verify(eventRepository).save(argThat(new EventMatcher(candidate.getDriverId(), AvatarType.DRIVER, EventType.HANDSHAKE)));
  }

  @Test
  public void sendGoOfflineToDriverSavesEvent() throws ServerError {
    final long driverId = 1L;

    testedInstance.sendGoOfflineToDriver(driverId, new HashMap<>());

    verify(eventRepository).save(argThat(new EventMatcher(driverId, AvatarType.DRIVER, EventType.GO_OFFLINE)));
  }

  @Test
  public void sendGoOfflineToDriverThrowsError() throws ServerError, JsonProcessingException {
    final long driverId = 1L;
    when(objectMapper.writeValueAsString(any())).thenThrow(new JsonMappingException("error"));

    expectedException.expect(ServerError.class);
    expectedException.expectMessage(EventsNotificationService.UNABLE_TO_SEND_EVENT_MESSAGE);

    testedInstance.sendGoOfflineToDriver(driverId, new HashMap<>());
  }

  @Test
  public void sendQueuedAreaEnteringToDriverSavesEvent() throws ServerError {
    final long driverId = 1L;

    testedInstance.sendQueuedAreaEnteringToDriver(driverId, new HashMap<>());

    verify(eventRepository).save(argThat(new EventMatcher(driverId, AvatarType.DRIVER, EventType.QUEUED_AREA_ENTERING)));
  }

  @Test
  public void sendQueuedAreaEnteringToDriverThrowsError() throws ServerError, JsonProcessingException {
    final long driverId = 1L;
    when(objectMapper.writeValueAsString(any())).thenThrow(new JsonMappingException("error"));

    expectedException.expect(ServerError.class);
    expectedException.expectMessage(EventsNotificationService.UNABLE_TO_SEND_EVENT_MESSAGE);

    testedInstance.sendQueuedAreaEnteringToDriver(driverId, new HashMap<>());
  }

  @Test
  public void sendQueuedAreaLeavingToDriverSavesEvent() throws ServerError {
    final long driverId = 1L;

    testedInstance.sendQueuedAreaLeavingToDriver(driverId, new HashMap<>());

    verify(eventRepository).save(argThat(new EventMatcher(driverId, AvatarType.DRIVER, EventType.QUEUED_AREA_LEAVING)));
  }

  @Test
  public void sendQueuedAreaLeavingToDriverThrowsError() throws ServerError, JsonProcessingException {
    final long driverId = 1L;
    when(objectMapper.writeValueAsString(any())).thenThrow(new JsonMappingException("error"));

    expectedException.expect(ServerError.class);
    expectedException.expectMessage(EventsNotificationService.UNABLE_TO_SEND_EVENT_MESSAGE);

    testedInstance.sendQueuedAreaLeavingToDriver(driverId, new HashMap<>());
  }

  @Test
  public void sendQueuedAreaGoingInactiveToDriverSavesEvent() throws ServerError {
    final long driverId = 1L;

    testedInstance.sendQueuedAreaGoingInactiveToDriver(driverId, new HashMap<>());

    verify(eventRepository).save(argThat(new EventMatcher(driverId, AvatarType.DRIVER, EventType.QUEUED_AREA_LEAVING_INACTIVE)));
  }

  @Test
  public void sendQueuedAreaGoingInactiveToDriverThrowsError() throws JsonProcessingException, ServerError {
    final long driverId = 1L;
    when(objectMapper.writeValueAsString(any())).thenThrow(new JsonMappingException("error"));

    expectedException.expect(ServerError.class);
    expectedException.expectMessage(EventsNotificationService.UNABLE_TO_SEND_EVENT_MESSAGE);

    testedInstance.sendQueuedAreaGoingInactiveToDriver(driverId, new HashMap<>());
  }

  @Test
  public void sendQueuedAreaPenalizedToDriverSavesEvent() throws ServerError {
    final long driverId = 1L;

    testedInstance.sendQueuedAreaPenalizedToDriver(driverId, new HashMap<>());

    verify(eventRepository).save(argThat(new EventMatcher(driverId, AvatarType.DRIVER, EventType.QUEUED_AREA_LEAVING_PENALTY)));
  }

  @Test
  public void sendQueuedAreaPenalizedToDriverThrowsError() throws JsonProcessingException, ServerError {
    final long driverId = 1L;
    when(objectMapper.writeValueAsString(any())).thenThrow(new JsonMappingException("error"));

    expectedException.expect(ServerError.class);
    expectedException.expectMessage(EventsNotificationService.UNABLE_TO_SEND_EVENT_MESSAGE);

    testedInstance.sendQueuedAreaPenalizedToDriver(driverId, new HashMap<>());
  }

  @Test
  public void sendQueuedAreaTakingRideToDriverSavesEvent() throws ServerError {
    final long driverId = 1L;

    testedInstance.sendQueuedAreaTakingRideToDriver(driverId, new HashMap<>());

    verify(eventRepository).save(argThat(new EventMatcher(driverId, AvatarType.DRIVER, EventType.QUEUED_AREA_LEAVING_RIDE)));
  }

  @Test
  public void sendQueuedAreaTakingRideToDriverThrowsError() throws ServerError, JsonProcessingException {
    final long driverId = 1L;
    when(objectMapper.writeValueAsString(any())).thenThrow(new JsonMappingException("error"));

    expectedException.expect(ServerError.class);
    expectedException.expectMessage(EventsNotificationService.UNABLE_TO_SEND_EVENT_MESSAGE);

    testedInstance.sendQueuedAreaTakingRideToDriver(driverId, new HashMap<>());
  }

  @Test
  public void sendQueuedAreaUpdateToDriverSavesEvent() throws ServerError {
    final long driverId = 1L;

    testedInstance.sendQueuedAreaUpdateToDriver(driverId, new HashMap<>());

    verify(eventRepository).save(argThat(new EventMatcher(driverId, AvatarType.DRIVER, EventType.QUEUED_AREA_UPDATE)));
  }

  @Test
  public void sendQueuedAreaUpdateToDriverThrowsError() throws JsonProcessingException, ServerError {
    final long driverId = 1L;
    when(objectMapper.writeValueAsString(any())).thenThrow(new JsonMappingException("error"));

    expectedException.expect(ServerError.class);
    expectedException.expectMessage(EventsNotificationService.UNABLE_TO_SEND_EVENT_MESSAGE);

    testedInstance.sendQueuedAreaUpdateToDriver(driverId, new HashMap<>());
  }

  @Test
  public void sendRatingUpdatedSavesEvent() {
    final long driverId = 1L;

    testedInstance.sendRatingUpdated(driverId, 5.0);

    verify(eventRepository).save(argThat(new EventMatcher(driverId, AvatarType.DRIVER, EventType.RATING_UPDATED)));
  }

  @Test
  public void sendCarCategoryChangeSavesEvent() throws ServerError {
    final long driverId = 1L;

    testedInstance.sendCarCategoryChange(driverId, Collections.emptyList());

    verify(eventRepository).save(argThat(new EventMatcher(driverId, AvatarType.DRIVER, EventType.CAR_CATEGORY_CHANGE)));
  }

  @Test
  public void sendCarCategoryChangeThrowsError() throws JsonProcessingException, ServerError {
    final long driverId = 1L;
    when(objectMapper.writeValueAsString(any())).thenThrow(new JsonMappingException("error"));

    expectedException.expect(ServerError.class);
    expectedException.expectMessage(EventsNotificationService.UNABLE_TO_SEND_EVENT_MESSAGE);

    testedInstance.sendCarCategoryChange(driverId, Collections.emptyList());
  }

  @Test
  public void sendCarCategoryAdminChangeSavesEvent() throws ServerError {
    final long driverId = 1L;

    testedInstance.sendCarCategoryChange(driverId);

    verify(eventRepository).save(argThat(new EventMatcher(driverId, AvatarType.DRIVER, EventType.CAR_CATEGORY_CHANGE)));
  }

  @Test
  public void sendCarCategoryAdminChangeThrowsError() throws JsonProcessingException, ServerError {
    final long driverId = 1L;
    when(objectMapper.writeValueAsString(any())).thenThrow(new JsonMappingException("error"));

    expectedException.expect(ServerError.class);
    expectedException.expectMessage(EventsNotificationService.UNABLE_TO_SEND_EVENT_MESSAGE);

    testedInstance.sendCarCategoryChange(driverId);
  }

  @Test
  public void sendDriverTypeChangeSavesEvent() {
    final long driverId = 1L;
    final Driver driver = new Driver();
    driver.setId(driverId);

    testedInstance.sendDriverTypeChange(driver);

    verify(eventRepository).save(argThat(new EventMatcher(driverId, AvatarType.DRIVER, EventType.DRIVER_TYPE_UPDATE)));
  }

  @Test
  public void sendRiderLocationUpdateSavesEvent() {
    final long driverId = 1L;

    testedInstance.sendRiderLocationUpdate(driverId, 34.681618, -97.81681, new Date().getTime());

    verify(eventRepository).save(argThat(new EventMatcher(driverId, AvatarType.DRIVER, EventType.RIDER_LOCATION_UPDATED)));
  }

  @Test
  public void sendRideUpgradeAcceptedSavesEvent() {
    final long driverId = 1L;
    final Ride ride = new Ride();

    testedInstance.sendRideUpgradeAccepted(ride, driverId);

    verify(eventRepository).save(argThat(new EventMatcher(ride, driverId, AvatarType.DRIVER, EventType.RIDE_UPGRADE_ACCEPTED)));
  }

  @Test
  public void sendRideUpgradeDeclinedSavesEvent() {
    final long driverId = 1L;

    testedInstance.sendRideUpgradeDeclined(1L, driverId);

    verify(eventRepository).save(argThat(new EventMatcher(driverId, AvatarType.DRIVER, EventType.RIDE_UPGRADE_DECLINED)));
  }

  @Test
  public void sendConfigurationItemChangedEventSavesEvent() {
    final long driverId = 1L;

    testedInstance.sendConfigurationItemChangedEvent(driverId, AvatarType.DRIVER, 1L, "a", "v", "c");

    verify(eventRepository).save(argThat(new EventMatcher(driverId, AvatarType.DRIVER, EventType.CONFIG_CHANGED)));
  }

  @Test
  public void sendConfigurationItemCreatedEventSavesEvent() {
    final long driverId = 1L;

    testedInstance.sendConfigurationItemCreatedEvent(driverId, AvatarType.DRIVER, 1L, "a", "v");

    verify(eventRepository).save(argThat(new EventMatcher(driverId, AvatarType.DRIVER, EventType.CONFIG_CREATED)));
  }

  private static class EventMatcher extends BaseMatcher<Event> {

    private final Ride ride;
    private final long avatarId;
    private final AvatarType avatarType;
    private final EventType eventType;

    public EventMatcher(long avatarId, AvatarType avatarType, EventType eventType) {
      this(null, avatarId, avatarType, eventType);
    }

    private EventMatcher(Ride ride, long avatarId, AvatarType avatarType, EventType eventType) {
      this.ride = ride;
      this.avatarId = avatarId;
      this.avatarType = avatarType;
      this.eventType = eventType;
    }

    @Override
    public boolean matches(Object o) {
      final Event event = (Event) o;
      return Objects.equals(event.getRide(), ride) && avatarId == event.getAvatarId() && avatarType == event.getAvatarType() &&
        eventType == event.getEventType();
    }

    @Override
    public void describeTo(Description description) {

    }
  }
}