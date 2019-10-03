package com.rideaustin.service.surgepricing;

import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.filter.ClientType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SurgeRecalculationConfigProvider {

  private static final String SURGE_CONFIG_KEY = "surgeConfig";

  private final ConfigurationItemCache configurationCache;

  public SurgeRecalculationConfig getConfig(Long cityId) {
    return configurationCache.getConfigurationForClient(ClientType.CONSOLE)
      .stream()
      .filter(c -> c.getCityId().equals(cityId) && c.getConfigurationKey().equals(SURGE_CONFIG_KEY))
      .findFirst()
      .map(c -> new SurgeRecalculationConfig((Map<String, Object>) c.getConfigurationObject()))
      .orElseGet(SurgeRecalculationConfig::new);
  }

}
