package com.rideaustin.driverstatistic.model;

import javax.inject.Inject;

import org.springframework.context.annotation.Configuration;

@Configuration
public class DriverStatisticDependencies {

  static DriverStatisticRepository repository;

  @Inject
  public DriverStatisticDependencies(DriverStatisticRepository repository) {
    DriverStatisticDependencies.repository = repository;
  }
}
