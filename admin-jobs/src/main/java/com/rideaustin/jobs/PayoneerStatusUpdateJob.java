package com.rideaustin.jobs;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.rideaustin.service.DriverAdministrationService;

@Component
public class PayoneerStatusUpdateJob extends BaseJob {

  private DriverAdministrationService driverService;

  private Boolean checkAllPendingDrivers = false;

  @Override
  protected String getDescription() {
    return "Payoneer status update";
  }

  @Override
  protected void executeInternal() {
    driverService.checkAndUpdatePayoneerStatusForPendingDrivers(checkAllPendingDrivers);
  }

  @Inject
  public void setDriverService(DriverAdministrationService driverService) {
    this.driverService = driverService;
  }

  public void setCheckAllPendingDrivers(Boolean checkAllPendingDrivers) {
    this.checkAllPendingDrivers = checkAllPendingDrivers;
  }
}
