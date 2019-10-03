package com.rideaustin.jobs;

import javax.inject.Inject;

import com.rideaustin.service.ride.RideUpgradeService;

public class RideUpgradeRequestExpirationJob extends BaseJob {

  private RideUpgradeService upgradeService;

  @Override
  protected void executeInternal() {
    upgradeService.expireRequests();
  }

  @Override
  protected String getDescription() {
    return "Expiring old ride upgrade requests";
  }

  @Inject
  public void setUpgradeService(RideUpgradeService upgradeService) {
    this.upgradeService = upgradeService;
  }
}
