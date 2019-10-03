package com.rideaustin.clients.configuration.elements;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.rideaustin.clients.configuration.ConfigurationElement;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.Campaign;
import com.rideaustin.model.user.Rider;
import com.rideaustin.repo.dsl.ConfigurationItemDslRepository;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.rest.model.Location;
import com.rideaustin.service.CampaignService;
import com.rideaustin.service.CurrentUserService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CampaignAccessibilityConfigurationElement implements ConfigurationElement {

  static final String CONFIGURATION_KEY = "accessibility";
  private final CampaignService campaignService;
  private final ObjectMapper mapper;
  private final ConfigurationItemDslRepository repository;
  private final CurrentUserService currentUserService;

  @Override
  public Map getConfiguration(ClientType clientType, Location location, Long cityId) throws RideAustinException {
    if (clientType != ClientType.RIDER) {
      return Collections.emptyMap();
    }
    String config = repository.findByKeyAndCityId(CONFIGURATION_KEY, cityId).getConfigurationValue();
    Optional<Campaign> campaign = location == null || location.getLat() == null || location.getLng() == null
      ? Optional.empty()
      : campaignService.findEligibleCampaign(new Date(), location.asLatLng(), "REGULAR", currentUserService.getUser().getAvatar(Rider.class));
    try {
      final Optional<String> accessibilityConfig = campaign.map(Campaign::getAccessibilityConfig);
      if (accessibilityConfig.isPresent()) {
        config = accessibilityConfig.get();
      }
      return ImmutableMap.of(CONFIGURATION_KEY, mapper.readValue(config, Map.class));
    } catch (IOException e) {
      throw new ServerError(e.getMessage());
    }
  }

  @Override
  public Map getDefaultConfiguration(ClientType clientType, Location location, Long cityId) throws RideAustinException {
    return getConfiguration(clientType, location, cityId);
  }
}
