package com.rideaustin.test.stubs;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.Constants;
import com.rideaustin.clients.configuration.ConfigurationItemService;
import com.rideaustin.filter.ClientType;

public class ConfigurationItemCache extends com.rideaustin.clients.configuration.ConfigurationItemCache {

  private Map<Long, Map<ClientType, Map<String, Map<String, Object>>>> config = new HashMap<>();

  public ConfigurationItemCache(ConfigurationItemService configurationItemService, ObjectMapper objectMapper) {
    super(configurationItemService, objectMapper);
  }

  public void setBooleanConfig(Long cityId, ClientType clientType, String configKey, String field, Boolean value) {
    setConfigValue(cityId, clientType, configKey, field, value);
  }

  public void setIntConfig(Long cityId, ClientType clientType, String configKey, String field, int value) {
    setConfigValue(cityId, clientType, configKey, field, value);
  }

  private <T> void setConfigValue(Long cityId, ClientType clientType, String configKey, String field, T value) {
    cityId = Optional.ofNullable(cityId).orElse(Constants.DEFAULT_CITY_ID);
    if (config.containsKey(cityId)) {
      if (config.get(cityId).containsKey(clientType)) {
        if (config.get(cityId).get(clientType).containsKey(configKey)) {
          config.get(cityId).get(clientType).get(configKey).put(field, value);
        } else {
          HashMap<String, Object> inner = new HashMap<>();
          inner.put(field, value);
          config.get(cityId).get(clientType).put(configKey, inner);
        }
      } else {
        HashMap<String, Map<String, Object>> inner = new HashMap<>();
        HashMap<String, Object> inner1 = new HashMap<>();
        inner1.put(field, value);
        inner.put(configKey, inner1);
        config.get(cityId).put(clientType, inner);
      }
    } else {
      Map<ClientType, Map<String, Map<String, Object>>> inner = new EnumMap<>(ClientType.class);
      HashMap<String, Map<String, Object>> inner1 = new HashMap<>();
      HashMap<String, Object> inner2 = new HashMap<>();
      inner2.put(field, value);
      inner1.put(configKey, inner2);
      inner.put(clientType, inner1);
      config.put(cityId, inner);
    }
  }

  @Override
  public boolean getConfigAsBoolean(ClientType clientType, String configurationKey, String fieldName, Long cityId) {
    return (boolean) config.get(Optional.ofNullable(cityId).orElse(Constants.DEFAULT_CITY_ID)).get(clientType).get(configurationKey).get(fieldName);
  }

  @Override
  public Integer getConfigAsInt(ClientType clientType, String configurationKey, String fieldName, Long cityId) {
    final Object value = config.get(Optional.ofNullable(cityId).orElse(Constants.DEFAULT_CITY_ID))
      .getOrDefault(clientType, new HashMap<>())
      .getOrDefault(configurationKey, new HashMap<>())
      .get(fieldName);
    if (value == null) {
      return super.getConfigAsInt(clientType, configurationKey, fieldName, cityId);
    }
    return (Integer) value;
  }
}
