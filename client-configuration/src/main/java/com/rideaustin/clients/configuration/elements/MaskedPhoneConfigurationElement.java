package com.rideaustin.clients.configuration.elements;

import java.util.Map;

import javax.inject.Inject;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.clients.configuration.ConfigurationElement;
import com.rideaustin.filter.ClientType;
import com.rideaustin.rest.model.Location;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MaskedPhoneConfigurationElement implements ConfigurationElement {

  private final Environment environment;

  @Override
  public Map getConfiguration(ClientType clientType, Location location, Long cityId) {
    return getDefaultConfiguration(clientType, location, cityId);
  }

  @Override
  public Map getDefaultConfiguration(ClientType clientType, Location location, Long cityId) {
    return ImmutableMap.of("directConnectPhone", environment.getProperty("sms.twilio.sender"));
  }
}
