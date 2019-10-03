package com.rideaustin.service.event;

import java.util.List;

import javax.inject.Inject;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.rideaustin.clients.configuration.events.ConfigurationItemChangedEvent;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.repo.dsl.DriverDslRepository;
import com.rideaustin.rest.model.ListDriversParams;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ConfigurationItemChangedEventListener {

  private final EventsNotificationService eventsNotificationService;
  private final DriverDslRepository driverDslRepository;

  /**
   * The concept here allows for @Async handling of event.
   * Consider, based on performance indicators.
   */
  @EventListener
  public void handle(ConfigurationItemChangedEvent event) {

    ClientType clientType = event.getClientType();

    if (ClientType.DRIVER.equals(clientType)) {
      List<Long> driverIds = driverDslRepository.findDriverIds(new ListDriversParams());

      driverIds.forEach(driverId -> eventsNotificationService.sendConfigurationItemChangedEvent(driverId,
        AvatarType.DRIVER,
        event.getConfigurationItemId(),
        event.getConfigurationKey(),
        event.getOldValue(),
        event.getNewValue()));
    }
  }
}
