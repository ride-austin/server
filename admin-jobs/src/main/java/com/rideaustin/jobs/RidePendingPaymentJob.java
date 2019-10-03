package com.rideaustin.jobs;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.rideaustin.service.payment.PendingPaymentService;

@Component
public class RidePendingPaymentJob extends BaseJob {

  @Inject
  private PendingPaymentService pendingPaymentService;

  @Override
  protected void executeInternal() {
    pendingPaymentService.processPendingPaymentRides();
  }

  @Override
  protected String getDescription() {
    return "Retrying pending payment jobs";
  }

}
