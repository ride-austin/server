package com.rideaustin.clients.configuration.events;

import com.rideaustin.filter.ClientType;
import org.springframework.context.ApplicationEvent;

public class ConfigurationItemChangedEvent extends ApplicationEvent {

  private final Long configurationItemId;
  private final String configurationKey;
  private final ClientType clientType;
  private final String oldValue;
  private final String newValue;

  public ConfigurationItemChangedEvent(Object source, Long configurationItemId, String configurationKey, ClientType clientType, String oldValue, String newValue) {
    super(source);
    this.configurationItemId = configurationItemId;
    this.configurationKey = configurationKey;
    this.clientType = clientType;
    this.oldValue = oldValue;
    this.newValue = newValue;
  }

  public Long getConfigurationItemId() {
    return configurationItemId;
  }

  public ClientType getClientType() {
    return clientType;
  }

  public String getOldValue() {
    return oldValue;
  }

  public String getNewValue() {
    return newValue;
  }

  public String getConfigurationKey() {
    return configurationKey;
  }

}
