package com.rideaustin.service.config;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.filter.ClientType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class StackedRidesConfig {

  private static final String CONFIGURATION_KEY = "stackedRides";

  private final ConfigurationItemCache configurationItemCache;

  public Integer getEndRideTimeThreshold(long cityId) {
    return configurationItemCache.getConfigAsInt(ClientType.CONSOLE, CONFIGURATION_KEY, "endRideTimeThreshold", cityId);
  }

  public boolean isStackingEnabled(long cityId) {
    return configurationItemCache.getConfigAsBoolean(ClientType.CONSOLE, CONFIGURATION_KEY, "enabled", cityId);
  }

  public int getStackingDropoffExpectation() {
    return configurationItemCache.getConfigAsInt(ClientType.CONSOLE, CONFIGURATION_KEY, "dropoffExpectationTime");
  }

  public boolean isForceRedispatchEnabled(long cityId) {
    return configurationItemCache.getConfigAsBoolean(ClientType.CONSOLE, CONFIGURATION_KEY, "forceRedispatchEnabled", cityId);
  }
}
