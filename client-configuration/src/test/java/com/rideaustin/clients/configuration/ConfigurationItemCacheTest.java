package com.rideaustin.clients.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.ConfigurationItem;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationItemCacheTest {

  private ConfigurationItemService configurationItemService = mock(ConfigurationItemService.class);

  private ObjectMapper objectMapper = new ObjectMapper();

  private ConfigurationItemCache configurationItemCache = new ConfigurationItemCache(configurationItemService, objectMapper);

  @Test
  public void shouldReturnIntConfig() throws Exception {
    // given
    final String configurationKey = "tipping";
    when(configurationItemService.findAll()).thenReturn(Collections.singletonList(
      ConfigurationItem.builder().clientType(ClientType.RIDER).cityId(null).configurationKey(configurationKey)
        .configurationValue("{\"rideTipLimit\": 300, \"ridePaymentDelay\": 60 } ").isDefault(true).environment(null).build()
    ));
    configurationItemCache.refreshCache();

    // when
    Integer actual = configurationItemCache.getConfigAsInt(ClientType.RIDER, configurationKey, "ridePaymentDelay");

    // then
    assertThat(actual).isEqualTo(60);
  }

  @Test
  public void shouldReturnIntConfig_WhenConfigHasNoCityId() throws Exception {
    // given
    final Long cityId = null;
    final String configurationKey = "tipping";
    when(configurationItemService.findAll()).thenReturn(Collections.singletonList(
      ConfigurationItem.builder().clientType(ClientType.RIDER).cityId(cityId).configurationKey(configurationKey)
        .configurationValue("{\"rideTipLimit\": 300, \"ridePaymentDelay\": 60 } ").isDefault(true).environment(null).build()
    ));
    configurationItemCache.refreshCache();

    // when
    final Long queryCityId = 1L;
    Integer actual = configurationItemCache.getConfigAsInt(ClientType.RIDER, configurationKey, "ridePaymentDelay", queryCityId);

    // then
    assertThat(actual).isEqualTo(60);
  }

  @Test
  public void shouldReturnIntConfig_WhenConfigItemHasCityId() throws Exception {
    // given
    final Long cityId = 1L;
    final String configurationKey = "tipping";
    when(configurationItemService.findAll()).thenReturn(Collections.singletonList(
      ConfigurationItem.builder().clientType(ClientType.RIDER).cityId(cityId).configurationKey(configurationKey)
        .configurationValue("{\"rideTipLimit\": 300, \"ridePaymentDelay\": 60 } ").isDefault(true).environment(null).build()
    ));
    configurationItemCache.refreshCache();

    // when
    final Long queryCityId = 1L;
    Integer actual = configurationItemCache.getConfigAsInt(ClientType.RIDER, configurationKey, "ridePaymentDelay", queryCityId);

    // then
    assertThat(actual).isEqualTo(60);
  }

  @Test
  public void shouldNotReturnIntConfig_WhenCityIdsDoNotMatch() throws Exception {
    // given
    final Long cityId = 1L;
    final String configurationKey = "tipping";
    when(configurationItemService.findAll()).thenReturn(Collections.singletonList(
      ConfigurationItem.builder().clientType(ClientType.RIDER).cityId(cityId).configurationKey(configurationKey)
        .configurationValue("{\"rideTipLimit\": 300, \"ridePaymentDelay\": 60 } ").isDefault(true).environment(null).build()
    ));
    configurationItemCache.refreshCache();

    // when
    final Long queryCityId = 2L;
    Integer actual = configurationItemCache.getConfigAsInt(ClientType.RIDER, configurationKey, "ridePaymentDelay", queryCityId);

    // then
    assertThat(actual).isNull();
  }
}