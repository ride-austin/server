package com.rideaustin.service.payment;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.Session;
import com.rideaustin.model.enums.PaymentStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Rider;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.CurrentSessionService;
import com.rideaustin.service.PaymentJobService;
import com.rideaustin.service.notifications.PushNotificationsFacade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PendingPaymentService {

  private final RideDslRepository rideDslRepository;
  private final PaymentService paymentService;
  private final CurrentSessionService currentSessionService;
  private final PushNotificationsFacade pushNotificationsFacade;
  private final PaymentJobService rideJobService;

  public boolean handlePendingPayments(Rider rider) throws RideAustinException {
    boolean paid = true;
    if (PaymentStatus.UNPAID.equals(rider.getPaymentStatus())) {
      List<Ride> unpaidRides = rideDslRepository.listPendingPaymentsRides(rider);
      for (Ride unpaidRide : unpaidRides) {
        if (!paymentService.processRidePayment(unpaidRide)) {
          paid = false;
        }
      }
      if (CollectionUtils.isEmpty(unpaidRides)) {
        rider.setPaymentStatus(PaymentStatus.PAID);
        Session session = currentSessionService.getCurrentSession(rider.getUser());
        pushNotificationsFacade.pushRiderPaymentUpdate(rider.getUser(), PaymentStatus.PAID, session);
      }
    }
    return paid;
  }

  public void processPendingPaymentRides() {
    try {
      rideDslRepository.listPendingPaymentsRides(null).stream()
        .map(Ride::getId)
        .forEach(rideJobService::immediatelyTriggerPaymentJob);
    } catch (Exception e) {
      log.error("Exception occurred", e);
    }
  }
}
