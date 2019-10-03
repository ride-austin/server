package com.rideaustin.clients.configuration.events;

import com.rideaustin.filter.ClientType;
import org.springframework.context.ApplicationEvent;

public class ConfigurationItemCreatedEvent extends ApplicationEvent {

  private final Long configurationItemId;
  private final String configurationKey;
  private final ClientType clientType;
  private final String value;

  public ConfigurationItemCreatedEvent(Object source, Long configurationItemId, String configurationKey, ClientType clientType, String value) {
    super(source);
    this.configurationItemId = configurationItemId;
    this.configurationKey = configurationKey;
    this.clientType = clientType;
    this.value = value;
  }

  public Long getConfigurationItemId() {
    return configurationItemId;
  }

  public ClientType getClientType() {
    return clientType;
  }

  public String getValue() {
    return value;
  }

  public String getConfigurationKey() {
    return configurationKey;
  }

}
