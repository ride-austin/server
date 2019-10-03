package com.rideaustin.model;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

public interface JsonConfigurable<T> {

  default T getConfigurationObject(ObjectMapper objectMapper) {
    return getConfigurationObject(objectMapper, getConfigurationClass());
  }

  default <E extends T> E getConfigurationObject(ObjectMapper objectMapper, Class<E> concreteClass) {
    E config = (E) getDefaultConfiguration();
    if (getConfiguration() != null) {
      try {
        config = objectMapper.readValue(getConfiguration(), concreteClass);
      } catch (IOException e) {
        config = (E) getDefaultConfiguration();
        LoggingFacility.log.error("Failed to parse configuration", e);
      }
    }
    return config;
  }

  T getDefaultConfiguration();

  Class<? extends T> getConfigurationClass();

  String getConfiguration();

  @Slf4j
  class LoggingFacility {

  }
}
