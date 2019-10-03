package com.rideaustin.assemblers;

import java.io.IOException;
import java.util.HashMap;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.model.ConfigurationItem;
import com.rideaustin.rest.model.ConfigurationItemDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ConfigurationItemDtoAssembler implements SingleSideAssembler<ConfigurationItem, ConfigurationItemDto> {

  private final ObjectMapper objectMapper;
  private final TypeReference<HashMap<String, Object>> typeReference;

  @Inject
  public ConfigurationItemDtoAssembler(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    typeReference = new TypeReference<HashMap<String, Object>>() {
    };
  }


  @Override
  public ConfigurationItemDto toDto(ConfigurationItem configurationItem) {

    String value = configurationItem.getConfigurationValue();

    Object objectValue;
    try {
      objectValue = objectMapper.readValue(value, typeReference);
    } catch (IOException e) {
      log.debug("unable to deserialize configuration item value {}", value, e);
      objectValue = value;
    }

    return new ConfigurationItemDto(
      configurationItem.getId(),
      configurationItem.getCityId(),
      configurationItem.getClientType(),
      configurationItem.getConfigurationKey(),
      objectValue,
      configurationItem.isDefault(),
      configurationItem.getEnvironment()
    );
  }
}
