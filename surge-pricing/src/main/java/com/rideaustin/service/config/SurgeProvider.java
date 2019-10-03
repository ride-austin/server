package com.rideaustin.service.config;

import com.rideaustin.service.surgepricing.SurgeRecalculationService;
import com.rideaustin.service.surgepricing.SurgeRecalculationServiceImpl;

public enum SurgeProvider {
  STATS(SurgeRecalculationServiceImpl.class);

  private final Class<? extends SurgeRecalculationService> implClass;

  SurgeProvider(Class<? extends SurgeRecalculationService> serviceClass) {
    this.implClass = serviceClass;
  }

  public Class<? extends SurgeRecalculationService> getImplClass() {
    return implClass;
  }
}
