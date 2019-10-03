package com.rideaustin.service.thirdparty;

import freemarker.template.Configuration;

public abstract class BaseCommunicationService implements CommunicationService {

  private final Configuration configuration;

  protected BaseCommunicationService(Configuration configuration) {
    this.configuration = configuration;
  }

  @Override
  public Configuration getConfiguration() {
    return configuration;
  }
}
