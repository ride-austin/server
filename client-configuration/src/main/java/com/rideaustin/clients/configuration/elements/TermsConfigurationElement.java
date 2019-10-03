package com.rideaustin.clients.configuration.elements;

import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.clients.configuration.ConfigurationElement;
import com.rideaustin.filter.ClientType;
import com.rideaustin.rest.model.Location;
import com.rideaustin.service.TermsService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TermsConfigurationElement implements ConfigurationElement {

  private final TermsService termsService;

  @Override
  public Map getConfiguration(ClientType clientType, Location location, Long cityId) {
    return ImmutableMap.of("currentTerms", termsService.getCurrentTerms(cityId));
  }

  @Override
  public Map getDefaultConfiguration(ClientType clientType, Location location, Long cityId) {
    return getConfiguration(clientType, location, cityId);
  }

}