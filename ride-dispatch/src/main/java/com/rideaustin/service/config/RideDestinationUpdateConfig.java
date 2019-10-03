package com.rideaustin.service.config;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.rideaustin.Constants;
import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.filter.ClientType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RideDestinationUpdateConfig {

  private static final String DESTINATION_UPDATE = "destinationUpdate";

  private final ConfigurationItemCache configurationItemCache;

  public boolean isDestinationUpdateLimited() {
    return configurationItemCache.getConfigAsBoolean(ClientType.CONSOLE, DESTINATION_UPDATE, "enabled", Constants.DEFAULT_CITY_ID);
  }

  public int getDestinationUpdateLimit() {
    return configurationItemCache.getConfigAsInt(ClientType.CONSOLE, DESTINATION_UPDATE, "limit", Constants.DEFAULT_CITY_ID);
  }
}
