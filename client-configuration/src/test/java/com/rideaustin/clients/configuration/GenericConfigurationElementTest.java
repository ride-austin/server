package com.rideaustin.clients.configuration;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.rideaustin.clients.configuration.elements.GenericConfigurationElement;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.ConfigurationItem;
import com.rideaustin.rest.model.Location;

@RunWith(MockitoJUnitRunner.class)
public class GenericConfigurationElementTest {

  private static final String KEY = "key";
  private static final String VALUE = "value";
  private static final String KEY_1 = "key1";
  private static final String KEY_3 = "key_3";
  private static final String KEY_4 = "key_4";
  @Mock
  private ConfigurationItemCache configurationItemCache;

  private GenericConfigurationElement genericConfigurationElement;

  @Before
  public void setup() {
    genericConfigurationElement = new
      GenericConfigurationElement(configurationItemCache);

  }

  @Test
  public void getConfigurationAllConfig() throws Exception {
    when(configurationItemCache.getConfigurationForClient(ClientType.DRIVER)).thenReturn(mockConfigurations());
    Map config = genericConfigurationElement.getConfiguration(ClientType.DRIVER, new Location(0d,0d), 1L);

    assertThat(config.get(KEY), is(notNullValue()));
    assertThat(config.get(KEY_1), is(notNullValue()));
  }

  @Test
  public void getConfigurationDefaultConfig() throws Exception {
    when(configurationItemCache.getConfigurationForClient(ClientType.DRIVER)).thenReturn(mockConfigurations());
    Map config = genericConfigurationElement.getDefaultConfiguration(ClientType.DRIVER, new Location(0d,0d), 1L);

    assertThat(config.get(KEY), is(notNullValue()));
    assertThat(config.get(KEY_1), is(nullValue()));
  }

  @Test
  public void getConfigurationPerCity() throws Exception {
    when(configurationItemCache.getConfigurationForClient(ClientType.DRIVER)).thenReturn(mockConfigurations());
    Map config = genericConfigurationElement.getConfiguration(ClientType.DRIVER, new Location(0d,0d), 2L);

    assertThat(config.get(KEY_3), is(notNullValue()));
    assertThat(config.get(KEY_4), is(notNullValue()));
  }

  private List<ConfigurationItem> mockConfigurations() {
    return Lists.newArrayList(
      mockConfigurationItem(KEY, VALUE, true, 1L),
      mockConfigurationItem(KEY_1, VALUE, false, 1L),
      mockConfigurationItem(KEY_3, VALUE, true, 2L),
      mockConfigurationItem(KEY_4, VALUE, false, 2L));

  }

  private ConfigurationItem mockConfigurationItem(String key, String value, Boolean def, Long cityId) {
    ConfigurationItem item1 = new ConfigurationItem();
    item1.setConfigurationKey(key);
    item1.setConfigurationValue(value);
    item1.setDefault(def);
    item1.setCityId(cityId);
    item1.setConfigurationObject(ImmutableMap.of(key, value));
    return item1;
  }

}