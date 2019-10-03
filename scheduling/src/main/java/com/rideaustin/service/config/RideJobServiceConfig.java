package com.rideaustin.service.config;

import javax.inject.Inject;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.filter.ClientType;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RideJobServiceConfig {

  @Getter
  private final Long preponeDeclinedDriverDispatchesThreshold;
  private final Integer ridePaymentDelay;
  private final ConfigurationItemCache configurationItemCache;

  @Inject
  public RideJobServiceConfig(Environment environment, ConfigurationItemCache configurationItemCache) {
    preponeDeclinedDriverDispatchesThreshold =
      environment.getProperty("dispatch.prepone_declined_driver_dispatches_threshold", Long.class, 2000L);
    ridePaymentDelay = environment.getProperty("ride.payment_delay_period", Integer.class);
    this.configurationItemCache = configurationItemCache;
  }

  public Integer getRidePaymentDelay() {
    Integer paymentDelayFromConf = configurationItemCache.getConfigAsInt(ClientType.RIDER, "tipping", "ridePaymentDelay");
    if (paymentDelayFromConf != null) {
      return paymentDelayFromConf;
    }
    return ridePaymentDelay;
  }


}
