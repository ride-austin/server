package com.rideaustin.service.config;

import javax.inject.Inject;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
@Getter
public class SurgePricingServiceConfig {

  private final Integer riderNotificationUpdateInterval;
  private final Integer inactiveDriversNotificationInterval;

  @Inject
  public SurgePricingServiceConfig(Environment environment) {
    this.riderNotificationUpdateInterval =
      environment.getProperty("surge_pricing.rider_notification_update_interval", Integer.class, 600);
    this.inactiveDriversNotificationInterval =
      environment.getProperty("surge_pricing.inactive_drivers_notification_interval", Integer.class, 6);
  }

}
