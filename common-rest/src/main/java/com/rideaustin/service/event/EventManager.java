package com.rideaustin.service.event;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.CheckedTransactional;
import com.rideaustin.assemblers.EventDtoAssembler;
import com.rideaustin.assemblers.MobileDriverRideDtoEnricher;
import com.rideaustin.model.Event;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.user.Avatar.Info;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.EventDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.model.EventDto;
import com.rideaustin.rest.model.EventScheduledUser;
import com.rideaustin.rest.model.MobileDriverRideDto;
import com.rideaustin.service.generic.TimeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@CheckedTransactional
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class EventManager {

  private final Map<Long, EventScheduledUser> registeredDrivers = new ConcurrentHashMap<>();
  private final Map<Long, EventScheduledUser> registeredRiders = new ConcurrentHashMap<>();

  private final EventDslRepository repository;
  private final TimeService timeService;
  private final EventDtoAssembler eventDtoAssembler;

  private final RideDslRepository rideDslRepository;
  private final MobileDriverRideDtoEnricher rideEnricher;

  private final ObjectMapper mapper;

  @Scheduled(fixedDelayString = "500", initialDelayString = "${events.initial_delay:0}")
  public void refreshEvents() {
    removeExpiredLongPollingRequests(registeredDrivers);
    removeExpiredLongPollingRequests(registeredRiders);

    Iterable<Event> driversEvents = repository.listEvents(registeredDrivers.keySet(), AvatarType.DRIVER);
    Iterable<Event> ridersEvents = repository.listEvents(registeredRiders.keySet(), AvatarType.RIDER);

    List<Event> toRemoveEvents = new ArrayList<>();

    processEvents(driversEvents, registeredDrivers, toRemoveEvents);
    processEvents(ridersEvents, registeredRiders, toRemoveEvents);
    repository.deleteInBatch(toRemoveEvents);
  }

  public void cleanExpiredEvents() {
    repository.deleteExpiredEvents(timeService.getCurrentDate());
  }

  private void removeExpiredLongPollingRequests(
    final Map<Long, EventScheduledUser> registered) {
    registered.entrySet().iterator().forEachRemaining(a -> {
      if (a.getValue().getDeferredResult().isSetOrExpired()) {
        registered.remove(a.getKey());
      }
    });
  }

  private void processEvents(Iterable<Event> newEvents,
    Map<Long, EventScheduledUser> registered, List<Event> toRemoveEvents) {
    Map<Long, List<Event>> eventsMap = StreamSupport.stream(newEvents.spliterator(), false)
      .collect(Collectors.groupingBy(Event::getAvatarId));
    eventsMap.forEach((userId, events) -> processUser(registered, toRemoveEvents, userId, events));
  }

  private void processUser(Map<Long, EventScheduledUser> registered, List<Event> toRemoveEvents, Long userId, List<Event> events) {
    EventScheduledUser resultWrapper = registered.get(userId);
    if (resultWrapper != null && !resultWrapper.getDeferredResult().isSetOrExpired() && CollectionUtils.isNotEmpty(events)) {
      registered.remove(userId);
      Date currentDate = timeService.getCurrentDate();

      Stream<Event> validEventsStream = events.stream()
        .filter(e -> e.getExpiresOn().after(currentDate));
      if (resultWrapper.getLastReceivedEvent() != null) {
        validEventsStream = validEventsStream.filter(e -> e.getId() > resultWrapper.getLastReceivedEvent());
      }

      List<Event> validEvents = validEventsStream.collect(Collectors.toList());
      List<EventDto> eventDtoList = new ArrayList<>();
      validEvents.forEach(e -> {
        EventDto dto = eventDtoAssembler.toDto(e);
        if (e.getRide() != null) {
          MobileDriverRideDto ride = rideDslRepository.findOneForDriverEvent(e.getRide().getId());
          ride = rideEnricher.enrich(ride);
          dto.setRide(ride, e.getParameterObject(mapper));
        }
        eventDtoList.add(dto);
      });
      resultWrapper.getDeferredResult().setResult(eventDtoList);
      if (resultWrapper.getLastReceivedEvent() != null) {
        events.removeAll(validEvents);
      }
      toRemoveEvents.addAll(events);
    }
  }

  public void register(Info info, User user, Long lastReceivedEvent, DeferredResult<List<EventDto>> deferredResult) {
    if (info == null) {
      deferredResult.setResult(new ArrayList<>());
      return;
    }
    AvatarType avatarType = info.getType();
    if (avatarType == AvatarType.DRIVER) {
      registerDriver(info.getId(), new EventScheduledUser(user, lastReceivedEvent, deferredResult));
    } else if (avatarType == AvatarType.RIDER) {
      registerRider(info.getId(), new EventScheduledUser(user, lastReceivedEvent, deferredResult));
    }
  }

  private void registerDriver(Long id, EventScheduledUser result) {
    registeredDrivers.put(id, result);
  }

  private void registerRider(Long id, EventScheduledUser result) {
    registeredRiders.put(id, result);
  }

  public void unregisterDriver(Long userId) {
    registeredDrivers.entrySet().removeIf(doUnregister(userId));

    if (isDriverRegistered(userId)) {
      log.error("===================== Driver has not been registered, userId: {}", userId);
    }
  }

  public void unregisterRider(Long userId) {
    registeredRiders.entrySet().removeIf(doUnregister(userId));
  }

  @NotNull
  private Predicate<Map.Entry<Long, EventScheduledUser>> doUnregister(Long userId) {
    return entry -> {
      EventScheduledUser value = entry.getValue();
      if (value.getUser().getId() == userId) {
        log.info("====================== Unregistering from listening for events - driver with userId: {}", userId);
        value.getDeferredResult().setResult(new ArrayList<>());
        return true;
      } else {
        return false;
      }
    };
  }

  private boolean isDriverRegistered(Long userId) {
    return registeredDrivers.values().stream()
      .anyMatch(eventScheduledUser -> eventScheduledUser.getUser().getId() == userId);
  }
}