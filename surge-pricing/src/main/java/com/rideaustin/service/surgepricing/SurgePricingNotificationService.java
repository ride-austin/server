package com.rideaustin.service.surgepricing;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import com.rideaustin.model.enums.EventType;
import com.rideaustin.model.surgepricing.SurgeArea;
import com.rideaustin.model.user.Avatar;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.repo.dsl.ActiveDriverDslRepository;
import com.rideaustin.repo.dsl.GeolocationLogDslRepository;
import com.rideaustin.repo.jpa.EventRepository;
import com.rideaustin.service.config.SurgePricingServiceConfig;
import com.rideaustin.service.event.EventBuilder;
import com.rideaustin.service.event.model.SurgeAreaUpdateContent;
import com.rideaustin.service.event.model.SurgeAreasUpdateEvent;
import com.rideaustin.service.generic.TimeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SurgePricingNotificationService {

  private final GeolocationLogDslRepository geolocationLogDslRepository;
  private final ActiveDriverDslRepository activeDriverDslRepository;
  private final EventRepository eventRepository;

  private final TimeService timeService;

  private final SurgePricingServiceConfig config;

  public void notifyUsers(SurgeArea surgeArea) {
    sendUpdateNotificationToDrivers(Collections.singleton(surgeArea));
    sendUpdateNotificationToRiders(Collections.singleton(surgeArea));
  }

  public void notifyUsers(Collection<SurgeArea> surgeAreas) {
    sendUpdateNotificationToDrivers(surgeAreas);
    sendUpdateNotificationToRiders(surgeAreas);
  }

  private void sendUpdateNotificationToDrivers(Collection<SurgeArea> surgeAreas) {
    Date inactivatedAfter = DateUtils.addHours(timeService.getCurrentDate(), -config.getInactiveDriversNotificationInterval());
    List<Driver> activeDrivers = activeDriverDslRepository.getActiveAndRecentlyActiveDrivers(inactivatedAfter);

    sendSurgeAreaUpdate(activeDrivers, surgeAreas);
  }

  private void sendUpdateNotificationToRiders(Collection<SurgeArea> surgeAreas) {
    Date dateFrom = DateUtils.addSeconds(timeService.getCurrentDate(), -config.getRiderNotificationUpdateInterval());

    Set<Rider> recentUsers = geolocationLogDslRepository.findRecentUsers(dateFrom);

    sendSurgeAreaUpdate(recentUsers, surgeAreas);
  }

  private <T extends Avatar> void sendSurgeAreaUpdate(Collection<T> avatars, Collection<SurgeArea> surgeAreas) {
    Set<Long> ids = new HashSet<>();
    for (T avatar : avatars) {
      if (ids.contains(avatar.getId())) {
        continue;
      }
      List<SurgeAreaUpdateContent> payload = surgeAreas.stream().map(SurgeAreaUpdateContent::new).collect(Collectors.toList());
      publishEvent(avatar, new SurgeAreasUpdateEvent(payload), EventType.SURGE_AREA_UPDATES);
      ids.add(avatar.getId());
    }
  }

  private <T extends Avatar> void publishEvent(T avatar, Object parameters, EventType eventType) {
    eventRepository.save(EventBuilder.create(avatar.getId(), avatar.getType(), eventType)
      .setExpirationPeriod(290000L)
      .setParameters(parameters)
      .get());
  }
}
