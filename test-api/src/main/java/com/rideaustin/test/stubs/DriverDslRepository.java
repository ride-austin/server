package com.rideaustin.test.stubs;

import javax.inject.Inject;

import com.rideaustin.service.DriverAuditedService;

public class DriverDslRepository extends com.rideaustin.repo.dsl.DriverDslRepository {

  @Inject
  public DriverDslRepository(DriverAuditedService driverAuditedService) {
    super(driverAuditedService);
  }

  @Override
  public String getLastDCID() {
    return "10000";
  }
}
