package com.rideaustin.clients.configuration;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.rideaustin.clients.configuration.elements.GenericConfigurationElement;
import com.rideaustin.filter.ClientType;
import com.rideaustin.rest.model.Location;

@RunWith(MockitoJUnitRunner.class)
public class ClientConfigurationServiceTest {

  private static final String KEY = "key";
  private static final String VALUE = "value";
  private static final String KEY_1 = "key1";
  private static final String VALUE_1 = "value1";

  @Mock
  private GenericConfigurationElement genericConfigurationElement;

  private ClientConfigurationService clientConfigurationService;

  @Before
  public void setup() {
    clientConfigurationService = new ClientConfigurationService(ImmutableList.of(genericConfigurationElement));
  }

  @Test
  public void getConfigurationBasicTest() throws Exception {
    when(genericConfigurationElement.getDefaultConfiguration(eq(ClientType.DRIVER), any(Location.class), eq(1L))).thenReturn(mockConfig());

    Map<String, Object> config = clientConfigurationService.getConfiguration(ClientType.DRIVER, new Location(0d,0d), 1L, null);
    assertThat(config.get(KEY), is(VALUE));
    assertThat(config.get(KEY_1), is(VALUE_1));
  }

  @Test
  public void getConfigurationFiltering() throws Exception {
    when(genericConfigurationElement.getConfiguration(ClientType.DRIVER, new Location(0d,0d), 1L)).thenReturn(mockConfig());

    Map<String, Object> config = clientConfigurationService.getConfiguration(ClientType.DRIVER, new Location(0d,0d), 1L, Sets.newHashSet(KEY));
    assertThat(config.get(KEY_1), is(nullValue()));
  }

  private Map mockConfig() {
    return ImmutableMap.of(KEY, VALUE, KEY_1, VALUE_1);

  }

}