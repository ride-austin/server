package com.rideaustin.jobs;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.rideaustin.service.ActiveDriversService;
import com.rideaustin.service.recovery.ActiveDriversRecoveryService;

@Component
public class ActiveDriverDeactivateJob extends BaseJob {

  @Inject
  private ActiveDriversService activeDriversService;

  @Inject
  private ActiveDriversRecoveryService activeDriversRecoveryService;

  @Value("${jobs.active_driver_away.threshold_seconds}")
  private int locationUpdatedAwayThresholdSeconds;

  @Value("${jobs.active_driver_deactivate.threshold_seconds}")
  private int locationUpdatedInactiveThresholdSeconds;

  @Override
  protected String getDescription() {
    return "available drivers activity check";
  }

  @Override
  protected void executeInternal() throws JobExecutionException {
    Instant locationUpdatedLimit = Instant.now().minus(locationUpdatedAwayThresholdSeconds, ChronoUnit.SECONDS);
    activeDriversService.setAvailableDriversAway(Date.from(locationUpdatedLimit));

    try {
      locationUpdatedLimit = Instant.now().minus(locationUpdatedInactiveThresholdSeconds, ChronoUnit.SECONDS);
      activeDriversService.setAwayDriversInactive(Date.from(locationUpdatedLimit));
      activeDriversService.setAvailableDriversInactiveWhenTermNotAccepted();
    } catch (Exception e) {
      throw new JobExecutionException(e);
    }

    activeDriversRecoveryService.cleanUpRidingDrivers();
  }
}
