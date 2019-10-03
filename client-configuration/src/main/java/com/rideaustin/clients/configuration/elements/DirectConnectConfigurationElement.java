package com.rideaustin.clients.configuration.elements;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.clients.configuration.ConfigurationElement;
import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.filter.ClientType;
import com.rideaustin.rest.model.Location;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DirectConnectConfigurationElement implements ConfigurationElement {

  private static final String CONFIGURATION_KEY = "directConnect";
  private final ConfigurationItemCache configurationItemCache;

  @Override
  public Map getConfiguration(ClientType clientType, Location location, Long cityId) {
    return getDefaultConfiguration(clientType, location, cityId);
  }

  @Override
  public Map getDefaultConfiguration(ClientType clientType, Location location, Long cityId) {
    return configurationItemCache.getConfigurationForClient(clientType, CONFIGURATION_KEY, cityId)
      .map(item -> new HashMap<>(ImmutableMap.of(item.getConfigurationKey(), item.getConfigurationObject())))
      .orElse(new HashMap<>());
  }
}
