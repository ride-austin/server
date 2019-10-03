package com.rideaustin.service.event;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.rideaustin.jobs.BaseJob;

@Component
public class ExpiredEventsCleanUpJob extends BaseJob {

  @Inject
  private EventManager eventManager;

  @Override
  protected void executeInternal() {
    eventManager.cleanExpiredEvents();
  }

  @Override
  protected String getDescription() {
    return "Expired events cleanup job";
  }

}
