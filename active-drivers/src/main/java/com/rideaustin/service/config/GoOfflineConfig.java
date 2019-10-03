package com.rideaustin.service.config;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.filter.ClientType;
import com.rideaustin.service.ActiveDriversService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class GoOfflineConfig {

  private final ConfigurationItemCache configurationItemCache;

  public String getGoOfflineMessage(ActiveDriversService.GoOfflineEventSource source) {
    return configurationItemCache.getConfigAsString(ClientType.DRIVER, "offline", source.toString());
  }
}
