package com.rideaustin.clients.configuration;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.Constants;
import com.rideaustin.clients.configuration.events.ConfigurationItemChangedEvent;
import com.rideaustin.clients.configuration.events.ConfigurationItemCreatedEvent;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.ConfigurationItem;
import com.rideaustin.repo.dsl.ConfigurationItemDslRepository;

@Service
public class ConfigurationItemService {

  private final ConfigurationItemDslRepository configurationItemDslRepository;
  private final ObjectMapper objectMapper;

  private final String env;

  private final ApplicationEventPublisher publisher;

  @Inject
  public ConfigurationItemService(ConfigurationItemDslRepository configurationItemDslRepository, Environment environment, ObjectMapper objectMapper, ApplicationEventPublisher publisher) {
    this.configurationItemDslRepository = configurationItemDslRepository;
    this.objectMapper = objectMapper;
    this.env = environment.getProperty("environment", "DEV");
    this.publisher = publisher;
  }

  public List<ConfigurationItem> findAll() {
    return configurationItemDslRepository.findAll(env);
  }

  public ConfigurationItem findByKey(String propertyKey) {
    return configurationItemDslRepository.findByKey(propertyKey, env);
  }

  public ConfigurationItem findOne(Long id) throws ConfigurationItemNotFoundException {
    ConfigurationItem one = configurationItemDslRepository.findOne(id);
    if (one == null) {
      throw new ConfigurationItemNotFoundException(id);
    }
    return one;
  }

  public ConfigurationItem findByKeyAndCityId(String key, Long cityId) {
    return configurationItemDslRepository.findByKeyAndCityId(key, cityId);
  }

  public ConfigurationItem findByKeyClientAndCity(String key, ClientType clientType, Long cityId) {
    return configurationItemDslRepository.findByKeyClientAndCity(key, clientType, cityId);
  }

  @Transactional
  public boolean update(ConfigurationItem item, Map<String, Object> value) {

    String newValue = asJsonString(value);
    String oldValue = item.getConfigurationValue();

    if (oldValue != null && oldValue.equals(newValue)) {
      return false;
    }

    item.setConfigurationValue(newValue);
    configurationItemDslRepository.saveAny(item);
    publisher.publishEvent(new ConfigurationItemChangedEvent(this, item.getId(), item.getConfigurationKey(), item.getClientType(), oldValue, newValue));
    return true;
  }

  @Transactional
  public ConfigurationItem create(Constants.City city, ClientType clientType, String key, Map<String, Object> value, Boolean isDefault) {
    ConfigurationItem item = new ConfigurationItem();

    String configurationValue = asJsonString(value);

    item.setCityId(city.getId());
    item.setClientType(clientType);
    item.setConfigurationKey(key);
    item.setConfigurationValue(configurationValue);
    item.setDefault(isDefault);
    item.setConfigurationObject(null);
    ConfigurationItem configurationItem = configurationItemDslRepository.saveAny(item);
    publisher.publishEvent(new ConfigurationItemCreatedEvent(this, item.getId(), item.getConfigurationKey(), item.getClientType(), configurationValue));
    return configurationItem;
  }

  private String asJsonString(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException(String.format("unable to serialize json from: %s", value));
    }
  }

  @Transactional
  public void remove(Long id) {
    configurationItemDslRepository.remove(id);
  }
}
