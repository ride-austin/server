package com.rideaustin.dispatch.tasks;

import java.util.List;

import javax.inject.Inject;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

 import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.dsl.FarePaymentDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.SplitFareDto;
import com.rideaustin.service.payment.UpfrontPaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class UpfrontChargeTask implements Runnable {

  private Long rideId;

  private final UpfrontPaymentService paymentService;
  private final RideDslRepository rideDslRepository;
  private final FarePaymentDslRepository farePaymentDslRepository;

  @Override
  public void run() {
    if (rideId == null) {
      log.error("Failed to start upfront charge, ride ID is not set");
      return;
    }
    final Ride ride = rideDslRepository.findOne(rideId);
    if (ride.getEndLocationLat() == null || ride.getEndLocationLong() == null) {
      log.info(String.format("[Ride #%d][UPFRONT] Ride can't be charged upfront - destination is not set", rideId));
      return;
    }
    if (ride.getPaymentStatus() != null) {
      log.info(String.format("[Ride #%d][UPFRONT] Ride can't be charged upfront - ride is already charged", rideId));
      return;
    }
    final List<SplitFareDto> farePayments = farePaymentDslRepository.findFarePayments(rideId);
    if (!farePayments.isEmpty()) {
      log.info(String.format("[Ride #%d][UPFRONT] Ride can't be charged upfront - split fare is not supported", rideId));
      return;
    }
    if (ride.getStatus() == RideStatus.COMPLETED) {
      log.info(String.format("[Ride #%d][UPFRONT] Ride won't be charged upfront - ride is already completed and we'll wait for normal processing", rideId));
      return;
    }
    try {
      paymentService.processRidePayment(ride);
    } catch (RideAustinException e) {
      log.error(String.format("[Ride #%d][UPFRONT] Ride can't be charged upfront - error occurred", rideId), e);
    }
  }

  public UpfrontChargeTask withRideId(Long rideId) {
    this.rideId = rideId;
    return this;
  }
}
