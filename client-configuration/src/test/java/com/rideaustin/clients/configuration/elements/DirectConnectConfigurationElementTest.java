package com.rideaustin.clients.configuration.elements;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.ConfigurationItem;
import com.rideaustin.rest.model.Location;

public class DirectConnectConfigurationElementTest {

  @Mock
  private ConfigurationItemCache configurationItemCache;

  private DirectConnectConfigurationElement testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new DirectConnectConfigurationElement(configurationItemCache);
  }

  @Test
  public void getConfigurationReturnsStoredConfigAsMap() {
    final String configurationKey = "directConnect";
    final ConfigurationItem configurationItem = new ConfigurationItem();
    configurationItem.setConfigurationObject(ImmutableMap.of("enabled", true));
    configurationItem.setConfigurationKey(configurationKey);
    when(configurationItemCache.getConfigurationForClient(any(ClientType.class), eq(configurationKey), anyLong()))
      .thenReturn(Optional.of(configurationItem));

    final Map result = testedInstance.getConfiguration(ClientType.RIDER, new Location(), 1L);

    assertTrue(result.containsKey(configurationKey));
    assertTrue((boolean) ((Map<String, Object>) result.get(configurationKey)).get("enabled"));
  }
}