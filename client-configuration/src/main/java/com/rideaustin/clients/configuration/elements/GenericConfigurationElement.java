package com.rideaustin.clients.configuration.elements;

import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.clients.configuration.ConfigurationElement;
import com.rideaustin.filter.ClientType;
import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.rest.model.Location;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class GenericConfigurationElement implements ConfigurationElement {

  private final ConfigurationItemCache configurationItemCache;

  @Override
  public Map getConfiguration(ClientType clientType, Location location, Long cityId) {
    ImmutableMap.Builder builder = ImmutableMap.builder();
    configurationItemCache.getConfigurationForClient(clientType)
      .forEach(
        conf -> {
          if (conf.getCityId() == null || conf.getCityId().equals(cityId)) {
            builder.put(conf.getConfigurationKey(), conf.getConfigurationObject());
          }
        }
      );
    return builder.build();
  }

  @Override
  public Map getDefaultConfiguration(ClientType clientType, Location location, Long cityId) {
    ImmutableMap.Builder builder = ImmutableMap.builder();
    configurationItemCache.getConfigurationForClient(clientType)
      .forEach(
        conf -> {
          if (conf.isDefault() && (conf.getCityId() == null || conf.getCityId().equals(cityId))) {
            builder.put(conf.getConfigurationKey(), conf.getConfigurationObject());
          }
        }
      );
    return builder.build();
  }
}
