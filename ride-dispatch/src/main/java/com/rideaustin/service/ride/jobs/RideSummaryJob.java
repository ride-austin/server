package com.rideaustin.service.ride.jobs;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.rideaustin.jobs.BaseJob;
import com.rideaustin.service.RideSummaryService;

@Component
public class RideSummaryJob extends BaseJob {

  private RideSummaryService rideSummaryService;

  private Long rideId;

  @Override
  protected void executeInternal() {
    rideSummaryService.completeRide(rideId);
  }

  @Override
  protected String getDescription() {
    return "Summary for ride " + rideId;
  }

  @Inject
  public void setRideService(RideSummaryService rideSummaryService) {
    this.rideSummaryService = rideSummaryService;
  }

  public void setRideId(Long rideId) {
    this.rideId = rideId;
  }
}
