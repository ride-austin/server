package com.rideaustin.clients.configuration;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.rideaustin.filter.ClientType;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.Location;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ClientConfigurationService {

  private final List<ConfigurationElement> configurationElements;

  public Map<String, Object> getConfiguration(ClientType clientType, Location location, Long cityId, Set<String> configAttributes) throws RideAustinException {
    Map configMap;

    if (configAttributes == null) {
      configMap = getDefaultConfiguration(clientType, location, cityId);
    } else {
      configMap = getAllConfiguration(clientType, location, cityId);
      configMap.keySet().retainAll(configAttributes);
    }
    return configMap;
  }

  private Map getAllConfiguration(ClientType clientType, Location location, Long cityId) throws RideAustinException {
    Map<String, Object> config = Maps.newHashMap();
    for (ConfigurationElement configurationElement : configurationElements) {
      config.putAll(configurationElement.getConfiguration(clientType, location, cityId));
    }
    return config;
  }

  private Map getDefaultConfiguration(ClientType clientType, Location location, Long cityId) throws RideAustinException {
    Map<String, Object> config = Maps.newHashMap();
    for (ConfigurationElement configurationElement : configurationElements) {
      config.putAll(configurationElement.getDefaultConfiguration(clientType, location, cityId));
    }
    return config;
  }

}
