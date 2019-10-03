package com.rideaustin.service;

import java.util.Collections;

import javax.inject.Inject;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.rideaustin.service.ride.jobs.ProcessRidePaymentJob;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@Profile("!itest")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PaymentJobService {

  private static final String RIDE_ID_KEY = "rideId";
  private static final String RIDE_PAYMENT_JOB_NAME = "RidePayment";

  private final SchedulerService schedulerService;

  public void immediatelyTriggerPaymentJob(final long rideId) {
    try {
      schedulerService.triggerJob(ProcessRidePaymentJob.class,
        Long.toString(rideId), RIDE_PAYMENT_JOB_NAME,
        Collections.singletonMap(RIDE_ID_KEY, rideId));
    } catch (Exception e) {
      log.error("Exception occurred", e);
    }

  }

  public void reschedulePaymentJob(final long rideId) {
    afterCommit(() ->
      {
        try {
          if (schedulerService.checkIfExists(Long.toString(rideId), RIDE_PAYMENT_JOB_NAME)) {
            schedulerService.removeJob(Long.toString(rideId), RIDE_PAYMENT_JOB_NAME);
            schedulerService.triggerJob(ProcessRidePaymentJob.class,
              Long.toString(rideId), RIDE_PAYMENT_JOB_NAME,
              Collections.singletonMap(RIDE_ID_KEY, rideId));
          }
        } catch (Exception e) {
          throw new PaymentJobActionException(e);
        }
      }
    );
  }

  public void afterCommit(Action action) {
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
      @Override
      public void afterCommit() {
        try {
          action.run();
        } catch (PaymentJobActionException e) {
          log.error("Exception occurred", e);
        }
      }
    });
  }

  @FunctionalInterface
  public interface Action {
    void run() throws PaymentJobActionException;
  }

  public static class PaymentJobActionException extends Exception {
    PaymentJobActionException(Exception cause) {
      super(cause);
    }
  }
}
