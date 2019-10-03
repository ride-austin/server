package com.rideaustin.service.config;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.google.common.annotations.VisibleForTesting;
import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.filter.ClientType;

@Component
public class RideAcceptanceConfig {

  private final ConfigurationItemCache configurationItemCache;

  @VisibleForTesting
  static final String LATENCY_COVERAGE = "latencyCoverage";
  @VisibleForTesting
  static final String RIDE_ACCEPTANCE = "rideAcceptance";
  @VisibleForTesting
  static final String ACCEPTANCE_PERIOD = "acceptancePeriod";
  @VisibleForTesting
  static final String ALLOWANCE_PERIOD = "allowancePeriod";
  @VisibleForTesting
  static final String TOTAL_WAIT_TIME = "totalWaitTime";

  @Inject
  public RideAcceptanceConfig(ConfigurationItemCache configurationItemCache) {
    this.configurationItemCache = configurationItemCache;
  }

  public int getDriverAcceptancePeriod(Long cityId) {
    return configurationItemCache.getConfigAsInt(ClientType.DRIVER, RIDE_ACCEPTANCE, ACCEPTANCE_PERIOD, cityId);
  }

  public int getAllowancePeriod(Long cityId) {
    return configurationItemCache.getConfigAsInt(ClientType.DRIVER, RIDE_ACCEPTANCE, ALLOWANCE_PERIOD, cityId);
  }

  public int getPerDriverWaitPeriod(Long cityId) {
    return getDriverAcceptancePeriod(cityId) + getNetworkLatencyCoverage(cityId);
  }

  public int getNetworkLatencyCoverage(Long cityId) {
    return configurationItemCache.getConfigAsInt(ClientType.DRIVER, RIDE_ACCEPTANCE, LATENCY_COVERAGE, cityId);
  }

  public int getTotalWaitTime(Long cityId) {
    return configurationItemCache.getConfigAsInt(ClientType.DRIVER, RIDE_ACCEPTANCE, TOTAL_WAIT_TIME, cityId);
  }
}
