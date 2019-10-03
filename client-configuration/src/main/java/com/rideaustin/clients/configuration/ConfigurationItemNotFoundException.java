package com.rideaustin.clients.configuration;

public class ConfigurationItemNotFoundException extends Exception {
  ConfigurationItemNotFoundException(long id) {
    super(String.format("configuration item not found by id: %s", id));
  }
}
