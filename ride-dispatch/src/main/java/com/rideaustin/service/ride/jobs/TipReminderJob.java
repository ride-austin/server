package com.rideaustin.service.ride.jobs;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.rideaustin.jobs.BaseJob;
import com.rideaustin.service.TipReminderService;

@Component
public class TipReminderJob extends BaseJob {

  private TipReminderService tipReminderService;

  private Long rideId;

  @Override
  protected void executeInternal() {
    tipReminderService.sendTipReminderToRider(rideId);
  }

  @Override
  protected String getDescription() {
    return "Tip reminder for ride" + rideId;
  }

  @Inject
  public void setRideService(TipReminderService tipReminderService) {
    this.tipReminderService = tipReminderService;
  }

  public void setRideId(Long rideId) {
    this.rideId = rideId;
  }
}
