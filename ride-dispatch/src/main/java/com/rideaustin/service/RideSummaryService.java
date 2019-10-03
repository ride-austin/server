package com.rideaustin.service;

import java.util.Collections;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.amazonaws.services.sns.model.EndpointDisabledException;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.config.RideJobServiceConfig;
import com.rideaustin.service.notification.RideFlowPushNotificationFacade;
import com.rideaustin.service.ride.jobs.ProcessRidePaymentJob;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RideSummaryService {

  protected static final String RIDE_PAYMENT_JOB_NAME = "RidePayment";

  private final RideTrackerService rideTrackerService;
  private final RideDslRepository rideDslRepository;
  private final RideFlowPushNotificationFacade pushNotificationsFacade;
  private final FareService fareService;
  private final SchedulerService schedulerService;
  private final RideJobServiceConfig config;

  @Transactional
  public void completeRide(Long rideId) {
    Ride ride = rideDslRepository.findOne(rideId);
    if (ride == null) {
      return;
    }

    rideTrackerService.saveStaticImage(ride);

    fareService.calculateTotals(ride);

    ride = rideDslRepository.save(ride);
    try {
       pushNotificationsFacade.sendRideUpdateToRider(rideId);
    } catch (EndpointDisabledException e) {
      log.error(String.format("[Ride %d]Failed to notify rider upon ride completion", rideId), e);
    }
    try {
      schedulePaymentJob(ride.getId());
    } catch (ServerError e) {
      log.error("Failed to schedule payment job", e);
    }
  }

  private void schedulePaymentJob(final long rideId) throws ServerError {
    schedulerService.triggerJob(ProcessRidePaymentJob.class,
      Long.toString(rideId), RIDE_PAYMENT_JOB_NAME, config.getRidePaymentDelay(),
      Collections.singletonMap("rideId", rideId));
  }
}
