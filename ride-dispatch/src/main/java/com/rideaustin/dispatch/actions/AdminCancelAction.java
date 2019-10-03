package com.rideaustin.dispatch.actions;

import javax.inject.Inject;

import org.springframework.statemachine.StateContext;

import com.rideaustin.model.enums.PaymentStatus;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.splitfare.FarePayment;
import com.rideaustin.repo.dsl.FarePaymentDslRepository;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AdminCancelAction extends BaseCancelAction {

  @Inject
  private FarePaymentDslRepository farePaymentDslRepository;

  @Override
  protected RideStatus getStatus() {
    return RideStatus.ADMIN_CANCELLED;
  }

  @Override
  protected boolean shouldChargeCancellationFee(Ride ride, RideStatus sourceStatus, long activeDriverId) {
    return false;
  }

  @Override
  protected void makeRefund(Ride ride) throws RideAustinException {
    super.makeRefund(ride);
    if (ride.getStatus() == RideStatus.ACTIVE && ride.getPaymentStatus() == PaymentStatus.PREPAID_UPFRONT && ride.getChargeId() != null) {
      log.info(String.format("[Ride #%d] Refunding upfront charge", ride.getId()));
      stripeService.refundCharge(ride.getChargeId());
      ride.setPaymentStatus(null);
      final FarePayment farePayment = farePaymentDslRepository.findFarePayment(ride.getId(), ride.getRider().getId());
      if (farePayment != null) {
        farePayment.setPaymentStatus(null);
        farePaymentDslRepository.save(farePayment);
      }
    } else {
      log.info(String.format("[Ride #%d] Upfront charge can't be refunded. Status %s, payment status %s, upfront charge %s", ride.getId(), ride.getStatus(), ride.getPaymentStatus(), ride.getChargeId()));
    }
  }

  @Override
  protected void releaseActiveDriver(RideStatus status, long activeDriverId) {
    if (status == RideStatus.DRIVER_ASSIGNED) {
      log.info(String.format("[CANCEL] AD %d: Releasing AD for DA ride", activeDriverId));
      if (rideDslRepository.findPrecedingRide(activeDriverId) == null) {
        log.info(String.format("[CANCEL] AD %d: AD is in single ride, setting as available", activeDriverId));
        setRidingAsAvailable(activeDriverId);
      } else {
        log.info(String.format("[CANCEL] AD %d: AD is not in single ride, making stackable", activeDriverId));
        stackedDriverRegistry.makeStackable(activeDriverId);
      }
    } else if (status == RideStatus.DRIVER_REACHED) {
      log.info(String.format("[CANCEL] AD %d: Releasing AD for DR ride, setting as available", activeDriverId));
      setRidingAsAvailable(activeDriverId);
    } else if (status == RideStatus.ACTIVE) {
      log.info(String.format("[CANCEL] AD %d: Cancelling ACTIVE ride", activeDriverId));
      if (rideDslRepository.findNextRide(activeDriverId) == null) {
        log.info(String.format("[CANCEL] AD %d: Single ride, set as available", activeDriverId));
        setRidingAsAvailable(activeDriverId);
      } else {
        log.info(String.format("[CANCEL] AD %d: Not single ride, remove from stack", activeDriverId));
        stackedDriverRegistry.removeFromStack(activeDriverId);
      }
    } else {
      log.info("[CANCEL] AD " + activeDriverId + ": Remove from stackable");
      stackedDriverRegistry.removeFromStackable(activeDriverId);
    }
  }

  @Override
  protected void notifyRider(long id, StateContext<States, Events> context) {
    pushNotificationsFacade.sendRideUpdateToRider(id, RideStatus.ADMIN_CANCELLED);
  }

  @Override
  protected boolean isInSingleRide(long activeDriverId) {
    return rideDslRepository.findNextRide(activeDriverId) == null && rideDslRepository.findPrecedingRide(activeDriverId) == null;
  }
}
