package com.rideaustin.service.event;

import java.util.List;

import javax.inject.Inject;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.rideaustin.clients.configuration.events.ConfigurationItemCreatedEvent;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.repo.dsl.DriverDslRepository;
import com.rideaustin.rest.model.ListDriversParams;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ConfigurationItemCreatedEventListener {

  private final EventsNotificationService eventsNotificationService;
  private final DriverDslRepository driverDslRepository;

  @EventListener
  public void handle(ConfigurationItemCreatedEvent event) {

    ClientType clientType = event.getClientType();


    if (clientType.equals(ClientType.DRIVER)) {
      List<Long> driverIds = driverDslRepository.findDriverIds(new ListDriversParams());

      driverIds.forEach(driverId -> eventsNotificationService.sendConfigurationItemCreatedEvent(driverId,
        AvatarType.DRIVER,
        event.getConfigurationItemId(),
        event.getConfigurationKey(),
        event.getValue()));
    }
  }
}
