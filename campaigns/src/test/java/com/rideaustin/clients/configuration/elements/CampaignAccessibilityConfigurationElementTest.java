package com.rideaustin.clients.configuration.elements;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.maps.model.LatLng;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.Campaign;
import com.rideaustin.model.ConfigurationItem;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.ConfigurationItemDslRepository;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.rest.model.Location;
import com.rideaustin.service.CampaignService;
import com.rideaustin.service.CurrentUserService;

public class CampaignAccessibilityConfigurationElementTest {

  private ObjectMapper mapper = new ObjectMapper();
  @Mock
  private CampaignService campaignService;
  @Mock
  private ConfigurationItemDslRepository repository;
  @Mock
  private CurrentUserService currentUserService;

  private CampaignAccessibilityConfigurationElement testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new CampaignAccessibilityConfigurationElement(campaignService, mapper, repository, currentUserService);
  }

  @Test
  public void getConfigurationReturnsEmptyForNonRiders() throws RideAustinException {
    final Map result = testedInstance.getConfiguration(ClientType.DRIVER, null, null);

    assertTrue(result.isEmpty());
  }

  @Test
  public void getConfigurationReturnsDefaultMapOnEmptyLocation() throws RideAustinException {
    final long cityId = 1L;
    final ConfigurationItem item = new ConfigurationItem();
    item.setConfigurationValue("{\"a\":\"b\"}");
    when(repository.findByKeyAndCityId(anyString(), eq(cityId))).thenReturn(item);

    final Map result = testedInstance.getConfiguration(ClientType.RIDER, null, cityId);

    verify(campaignService, never()).findEligibleCampaign(any(Date.class), any(LatLng.class), anyString(), any(Rider.class));
    assertTrue(result.containsKey(CampaignAccessibilityConfigurationElement.CONFIGURATION_KEY));
    assertEquals("b", ((Map) result.get(CampaignAccessibilityConfigurationElement.CONFIGURATION_KEY)).get("a"));
  }

  @Test
  public void getConfigurationReturnsDefaultMapOnEmptyCampaign() throws RideAustinException {
    final long cityId = 1L;
    final ConfigurationItem item = new ConfigurationItem();
    item.setConfigurationValue("{\"a\":\"b\"}");
    when(repository.findByKeyAndCityId(anyString(), eq(cityId))).thenReturn(item);
    when(currentUserService.getUser()).thenReturn(new User());
    when(campaignService.findEligibleCampaign(any(Date.class), any(LatLng.class), anyString(), any(Rider.class)))
      .thenReturn(Optional.empty());

    final Map result = testedInstance.getConfiguration(ClientType.RIDER, new Location(new LatLng(34.64981, -97.94164)), cityId);

    assertTrue(result.containsKey(CampaignAccessibilityConfigurationElement.CONFIGURATION_KEY));
    assertEquals("b", ((Map) result.get(CampaignAccessibilityConfigurationElement.CONFIGURATION_KEY)).get("a"));
  }

  @Test
  public void getConfigurationReturnsCampaignMapOnPresentCampaign() throws RideAustinException {
    final long cityId = 1L;
    final ConfigurationItem item = new ConfigurationItem();
    final Campaign campaign = new Campaign();
    item.setConfigurationValue("{\"a\":\"b\"}");
    campaign.setAccessibilityConfig("{\"c\":\"d\"}");
    when(repository.findByKeyAndCityId(anyString(), eq(cityId))).thenReturn(item);
    when(currentUserService.getUser()).thenReturn(new User());
    when(campaignService.findEligibleCampaign(any(Date.class), any(LatLng.class), anyString(), any(Rider.class)))
      .thenReturn(Optional.of(campaign));

    final Map result = testedInstance.getConfiguration(ClientType.RIDER, new Location(new LatLng(34.64981, -97.94164)), cityId);

    assertTrue(result.containsKey(CampaignAccessibilityConfigurationElement.CONFIGURATION_KEY));
    assertEquals("d", ((Map) result.get(CampaignAccessibilityConfigurationElement.CONFIGURATION_KEY)).get("c"));
  }

  @Test(expected = ServerError.class)
  public void getConfigurationThrowsServerErrorOnMalformedConfig() throws RideAustinException {
    final long cityId = 1L;
    final ConfigurationItem item = new ConfigurationItem();
    item.setConfigurationValue("{\"a\":\"b\"");
    when(repository.findByKeyAndCityId(anyString(), eq(cityId))).thenReturn(item);

    final Map result = testedInstance.getConfiguration(ClientType.RIDER, null, cityId);
  }
}