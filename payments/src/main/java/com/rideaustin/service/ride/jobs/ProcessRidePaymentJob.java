package com.rideaustin.service.ride.jobs;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.rideaustin.jobs.BaseJob;
import com.rideaustin.jobs.JobExecutionException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.payment.PaymentService;

import lombok.Setter;

@Component
public class ProcessRidePaymentJob extends BaseJob {

  @Setter(onMethod = @__(@Inject))
  private PaymentService paymentService;

  @Setter
  private Long rideId;

  @Override
  protected void executeInternal() throws JobExecutionException {
    try {
      paymentService.processRidePayment(rideId);
    } catch (RideAustinException e) {
      throw new JobExecutionException(e);
    }
  }

  @Override
  protected String getDescription() {
    return "Payment for ride " + rideId;
  }

}
