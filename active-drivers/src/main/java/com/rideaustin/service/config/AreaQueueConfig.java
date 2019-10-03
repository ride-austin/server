package com.rideaustin.service.config;

import java.util.Optional;

import javax.inject.Inject;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.filter.ClientType;

import lombok.RequiredArgsConstructor;

@Component
@Profile("!itest")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AreaQueueConfig {

  private static final String QUEUE_CONFIGURATION_KEY = "queue";

  private final ConfigurationItemCache configurationItemCache;

  public int getInactiveTimeThresholdBeforeLeave() {
    return Optional.ofNullable(configurationItemCache.getConfigAsInt(ClientType.CONSOLE, QUEUE_CONFIGURATION_KEY, "inactiveBeforeLeave"))
      .orElse(10);
  }

  public int getOutOfAreaTimeThresholdBeforeLeave() {
    return Optional.ofNullable(configurationItemCache.getConfigAsInt(ClientType.CONSOLE, QUEUE_CONFIGURATION_KEY, "outOfAreaBeforeLeave"))
      .orElse(2);
  }

  public int getExclusionTimeThresholdBeforeLeave() {
    return Optional.ofNullable(configurationItemCache.getConfigAsInt(ClientType.CONSOLE, QUEUE_CONFIGURATION_KEY, "inExclusionBeforeLeave"))
      .orElse(10);
  }

  public int getMaxDeclines() {
    return Optional.ofNullable(configurationItemCache.getConfigAsInt(ClientType.CONSOLE, QUEUE_CONFIGURATION_KEY, "maxDeclines"))
      .orElse(2);
  }

  public int getPenaltyTimeout() {
    return Optional.ofNullable(configurationItemCache.getConfigAsInt(ClientType.CONSOLE, QUEUE_CONFIGURATION_KEY, "penaltyTimeoutSeconds"))
      .orElse(900);
  }

  public boolean isPenaltyEnabled() {
    return configurationItemCache.getConfigAsBoolean(ClientType.CONSOLE, QUEUE_CONFIGURATION_KEY, "penaltyEnabled", null);
  }
}
