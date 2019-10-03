package com.rideaustin.clients.configuration;

import static java.util.stream.Collectors.groupingBy;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.Maps;
import com.rideaustin.application.cache.CacheItem;
import com.rideaustin.application.cache.RefreshCacheException;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.ConfigurationItem;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile("!itest")
public class ConfigurationItemCache implements CacheItem {

  private static final String CONFIGURATION_ITEMS_CACHE = "configurationItemsCache";
  private final ConfigurationItemService configurationItemService;
  private final ObjectMapper objectMapper;
  private final MapType mapType;
  private Map<ClientType, List<ConfigurationItem>> configurationItems = Maps.newHashMap();

  @Inject
  public ConfigurationItemCache(ConfigurationItemService configurationItemService, ObjectMapper objectMapper) {
    this.configurationItemService = configurationItemService;
    this.objectMapper = objectMapper;
    TypeFactory typeFactory = objectMapper.getTypeFactory();
    mapType = typeFactory.constructMapType(HashMap.class, String.class, Object.class);
  }

  public List<ConfigurationItem> getConfigurationForClient(ClientType clientType) {
    return configurationItems.get(clientType);
  }

  @PostConstruct
  @Override
  public void refreshCache() throws RefreshCacheException {
    List<ConfigurationItem> newConfigurationItems = configurationItemService.findAll();
    for (ConfigurationItem item : newConfigurationItems) {
      try {
        item.setConfigurationObject(objectMapper.readValue(item.getConfigurationValue(), mapType));
      } catch (JsonMappingException e) {
        item.setConfigurationObject(handleLegacyValue(item));
      } catch (IOException e) {
        throw new RefreshCacheException(e);
      }
    }
    configurationItems = newConfigurationItems.stream().collect(groupingBy(ConfigurationItem::getClientType));
  }

  @Override
  public Map getAllCacheItems() {
    return configurationItems;
  }

  @Override
  public String getCacheName() {
    return CONFIGURATION_ITEMS_CACHE;
  }

  public Integer getConfigAsInt(ClientType clientType, String configurationKey, String fieldName) {
    return getConfigAsInt(clientType, configurationKey, fieldName, null);
  }

  public Integer getConfigAsInt(ClientType clientType, String configurationKey, String fieldName, Long cityId) {
    String value = getConfigAsString(clientType, configurationKey, fieldName, cityId);
    if (StringUtils.isNoneBlank(value)) {
      try {
        return Integer.parseInt(value);
      } catch (Exception e) {
        log.error("Unable to parse configuration item {}.{} ", configurationKey, fieldName);
      }
    }
    return null;
  }

  public Double getConfigAsDouble(ClientType clientType, String configurationKey, String fieldName) {
    String value = getConfigAsString(clientType, configurationKey, fieldName, null);
    if (StringUtils.isNoneBlank(value)) {
      try {
        return Double.valueOf(value);
      } catch (Exception e) {
        log.error("Unable to parse configuration item {}.{} ", configurationKey, fieldName);
      }
    }
    return null;
  }

  public String getConfigAsString(ClientType clientType, String configurationKey, String fieldName) {
    return getConfigAsString(clientType, configurationKey, fieldName, null);
  }

  public String getConfigAsString(ClientType clientType, String configurationKey, String fieldName, Long cityId) {
    return getConfig(clientType, configurationKey, fieldName, cityId, Object::toString, null);
  }

  public boolean getConfigAsBoolean(ClientType clientType, String configurationKey, String fieldName, Long cityId) {
    return getConfig(clientType, configurationKey, fieldName, cityId, o -> Boolean.valueOf(o.toString()), false);
  }

  public Optional<ConfigurationItem> getConfigurationForClient(ClientType clientType, String configurationKey, Long cityId) {
    List<ConfigurationItem> configurationForClient = getConfigurationForClient(clientType);
    if (configurationForClient != null) {
      return configurationForClient.stream()
        .filter(c -> configurationKey.equals(c.getConfigurationKey()))
        .filter(c -> cityId == null || c.getCityId() == null || cityId.equals(c.getCityId()))
        .findFirst();
    }
    return Optional.empty();
  }

  private <T> T getConfig(ClientType clientType, String configurationKey, String fieldName, Long cityId,
    Function<Object, T> mapper, T defaultValue) {
    Optional<ConfigurationItem> item = getConfigurationForClient(clientType, configurationKey, cityId);
    if (item.isPresent()) {
      Object configurationObject = item.get().getConfigurationObject();
      if (configurationObject instanceof Map) {
        Map<String, Object> valueMap = (Map<String, Object>) configurationObject;
        return Optional.ofNullable(valueMap.get(fieldName)).map(mapper).orElse(defaultValue);
      }
    }
    return defaultValue;
  }

  private Object handleLegacyValue(ConfigurationItem configurationItem) {
    if ("smsMaskingEnabled".equals(configurationItem.getConfigurationKey())) {
      return Boolean.parseBoolean(configurationItem.getConfigurationValue());
    }
    return null;
  }
}