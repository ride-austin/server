package com.rideaustin.service.payment;

import java.util.Date;

import javax.inject.Inject;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import com.rideaustin.model.Session;
import com.rideaustin.model.enums.PaymentProvider;
import com.rideaustin.model.enums.PaymentStatus;
import com.rideaustin.model.promocodes.Promocode;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.splitfare.FarePayment;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.CampaignService;
import com.rideaustin.service.CurrentSessionService;
import com.rideaustin.service.farepayment.FarePaymentService;
import com.rideaustin.service.notifications.PushNotificationsFacade;
import com.rideaustin.service.promocodes.PromocodeService;
import com.rideaustin.service.promocodes.PromocodeUseResult;
import com.rideaustin.service.thirdparty.StripeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public abstract class AbstractPaymentService {

  protected final CurrentSessionService currentSessionService;
  protected final StripeService stripeService;
  protected final RideDslRepository rideDslRepository;
  protected final PaymentEmailService paymentEmailService;
  protected final PromocodeService promocodeService;
  protected final FarePaymentService farePaymentService;
  protected final PushNotificationsFacade pushNotificationsFacade;
  protected final CampaignService campaignService;

  protected void handleSuccess(Ride ride, FarePaymentService.FarePaymentInfo farePaymentInfo, PromocodeUseResult promocodeUseResult, PaymentResult paymentResult) throws RideAustinException {
    stripeService.refundPreCharge(ride);
    ride.setPaymentStatus(PaymentStatus.PAID);
    if (rideDslRepository.listPendingPaymentsRides(ride.getRider()).isEmpty()) {
      ride.getRider().setPaymentStatus(PaymentStatus.PAID);
      Session session = currentSessionService.getCurrentSession(ride.getRider().getUser());
      notifyRider(ride, session, PaymentStatus.PAID);
    }
    Promocode promocodeUsed = null;
    if (promocodeUseResult.isSuccess() && promocodeUseResult.getAffectedPromocodeRedemption() != null) {
      promocodeUsed = promocodeUseResult.getAffectedPromocodeRedemption().getPromocode();
    }
    paymentEmailService.sendEndRideEmail(ride, farePaymentInfo.getPrimaryRiderFarePayment(),
      farePaymentInfo.getSecondaryRiderPayments(), promocodeUsed);
    unlockPayments(ride, paymentResult);
  }

  protected void handleFailure(Ride ride, FarePaymentService.FarePaymentInfo farePaymentInfo, PaymentResult paymentResult) throws RideAustinException {
    ride.setPaymentStatus(paymentResult.status);
    ride.getRider().setPaymentStatus(paymentResult.status);
    farePaymentInfo.getPrimaryRiderFarePayment().setChargeScheduled(DateUtils.addDays(new Date(), 1));
    farePaymentInfo.getPrimaryRiderFarePayment().setPaymentStatus(paymentResult.status);
    farePaymentService.updateFarePayment(farePaymentInfo.getPrimaryRiderFarePayment());
    for (FarePayment farePayment : farePaymentInfo.getSecondaryRiderPayments()) {
      farePayment.setChargeScheduled(DateUtils.addDays(new Date(), 1));
      farePaymentService.updateFarePayment(farePayment);
    }
    lockPayments(ride, paymentResult);
    Session session = currentSessionService.getCurrentSession(ride.getRider().getUser());
    notifyRider(ride, session, PaymentStatus.UNPAID);
  }

  protected void notifyRider(Ride ride, Session session, PaymentStatus status) {
    try {
      pushNotificationsFacade.pushRiderPaymentUpdate(ride.getRider().getUser(), status, session);
    } catch (Exception e) {
      log.error(String.format("[Payment][Ride #%d] Failed to send push notification", ride.getId()), e);
    }
  }

  protected abstract void lockPayments(Ride ride, PaymentResult paymentResult) throws RideAustinException;

  protected abstract void unlockPayments(Ride ride, PaymentResult paymentResult) throws RideAustinException;

  protected static class PaymentResult {
    boolean success = false;
    PaymentProvider method;
    PaymentStatus status = PaymentStatus.UNPAID;

    PaymentResult(String applePayToken) {
      method = applePayToken == null ? PaymentProvider.CREDIT_CARD : PaymentProvider.APPLE_PAY;
    }

    public PaymentResult(PaymentProvider method) {
      this.method = method;
    }

    PaymentResult() {}

    public PaymentResult(boolean success, PaymentProvider method, PaymentStatus status) {
      this.success = success;
      this.method = method;
      this.status = status;
    }
  }
}
