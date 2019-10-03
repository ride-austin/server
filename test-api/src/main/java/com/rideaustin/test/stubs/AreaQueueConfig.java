package com.rideaustin.test.stubs;

import com.rideaustin.clients.configuration.ConfigurationItemCache;

public class AreaQueueConfig extends com.rideaustin.service.config.AreaQueueConfig {

  public AreaQueueConfig(ConfigurationItemCache configurationItemCache) {
    super(configurationItemCache);
  }

  @Override
  public int getInactiveTimeThresholdBeforeLeave() {
    return 1;
  }

  @Override
  public int getOutOfAreaTimeThresholdBeforeLeave() {
    return 1;
  }

  @Override
  public int getExclusionTimeThresholdBeforeLeave() {
    return 1;
  }
}
