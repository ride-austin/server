package com.rideaustin.service.surgepricing;

import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.service.config.SurgeMode;

public interface SurgeRecalculationService {

  @Transactional
  void recalculateSurgePricingAreas(Long city);

  @Transactional
  boolean updateConfig(Long cityId, SurgeRecalculationConfig config);

  @Transactional
  boolean updatePriorityFareMode(Long cityId, SurgeMode surgeMode);

  class Config {
    static final String SURGE_CONFIG_KEY = "surgeConfig";

    private Config() {

    }
  }
}
