package com.rideaustin.jobs;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.rideaustin.service.promocodes.PromocodeService;

@Component
public class PromocodeRedemptionActivityJob extends BaseJob {

  @Inject
  private PromocodeService promocodeService;

  @Override
  protected void executeInternal() {
    promocodeService.deactivateExpiredRedemptions();
  }

  @Override
  protected String getDescription() {
    return "Deactivate expired promocode redemptions";
  }

}
