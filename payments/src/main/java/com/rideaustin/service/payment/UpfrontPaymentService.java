package com.rideaustin.service.payment;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.joda.money.Money;
import org.joda.money.MoneyUtils;
import org.springframework.stereotype.Service;

import com.rideaustin.model.Campaign;
import com.rideaustin.model.enums.PaymentProvider;
import com.rideaustin.model.enums.PaymentStatus;
import com.rideaustin.model.ride.FareDetails;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.splitfare.FarePayment;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.RiderCard;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.repo.dsl.RiderDslRepository;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.CampaignService;
import com.rideaustin.service.CurrentSessionService;
import com.rideaustin.service.FareService;
import com.rideaustin.service.RiderCardService;
import com.rideaustin.service.config.RidePaymentConfig;
import com.rideaustin.service.farepayment.FarePaymentService;
import com.rideaustin.service.notifications.PushNotificationsFacade;
import com.rideaustin.service.promocodes.PrevalidatedPromocodeUseRequest;
import com.rideaustin.service.promocodes.PromocodeService;
import com.rideaustin.service.promocodes.PromocodeUseResult;
import com.rideaustin.service.ride.RideLoadService;
import com.rideaustin.service.thirdparty.StripeService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UpfrontPaymentService extends PaymentService {

  public UpfrontPaymentService(CurrentSessionService currentSessionService, StripeService stripeService,
    RideDslRepository rideDslRepository, PaymentEmailService paymentEmailService,
    RiderDslRepository riderDslRepository, PromocodeService promocodeService, FarePaymentService farePaymentService,
    FareService fareService, RiderCardService riderCardService, RideLoadService rideLoadService,
    PushNotificationsFacade pushNotificationsFacade, CampaignService campaignService,
    RidePaymentConfig config) {
    super(currentSessionService, stripeService, rideDslRepository, paymentEmailService, riderDslRepository,
      promocodeService, farePaymentService, fareService, riderCardService, rideLoadService, pushNotificationsFacade,
      campaignService, config);
  }

  @Override
  protected PaymentResult performPrimaryPayment(FarePayment farePayment, Ride ride, Money primaryRiderFare, PromocodeUseResult freeCreditOverride, String applePayToken) throws RideAustinException {
    Rider rider = farePayment.getRider();
    String effectiveApplePayToken = Optional.ofNullable(applePayToken).orElse(ride.getApplePayToken());
    PaymentResult paymentResult = new PaymentResult(effectiveApplePayToken);
    RiderCard card = rider.getPrimaryCard();
    riderCardService.updateCardFingerprint(card);
    Optional<FareDetails> detailsOptional = fareService.calculateFinalFare(ride, freeCreditOverride, true);
    FareDetails fareDetails = detailsOptional.orElse(null);
    boolean tokenBasedPayment = StringUtils.isNotBlank(effectiveApplePayToken);
    PaymentStatus paymentStatus = PaymentStatus.PREPAID_UPFRONT;
    try {
      if (!detailsOptional.isPresent()) {
        throw new PaymentException("Ride can't be assessed");
      }
      ride.setFareDetails(fareDetails);
      if (!tokenBasedPayment && !validateCard(rider, card, ride)) {
        return paymentResult;
      }
      ride.setCharity(ride.getRider().getCharity());
      if (!MoneyUtils.isZero(primaryRiderFare)) {
        if (tokenBasedPayment) {
          try {
            log.info("Processing apple pay payment for ride #" + ride.getId());
            farePayment.setChargeId(stripeService.holdTokenPayment(rider, ride, primaryRiderFare));
            paymentResult.method = PaymentProvider.APPLE_PAY;
          } catch (RideAustinException ex) {
            log.error("Failed to perform apple pay", ex);
            tokenBasedPayment = false;
            if (validateCard(rider, card, ride)) {
              paymentResult.method = PaymentProvider.CREDIT_CARD;
              fareDetails = handleApplePayFailure(farePayment, ride, freeCreditOverride, rider, card);
              ride.setFareDetails(fareDetails);
            } else {
              throw ex;
            }
          }
        } else {
          log.info("Processing card payment for ride #" + ride.getId());
          farePayment.setChargeId(stripeService.holdCardPayment(rider, ride, card.getRider(), card.getStripeCardId(), primaryRiderFare));
          card.resetFailureCount();
          riderCardService.updateRiderCard(card);
        }
      }
      paymentResult.success = true;
      paymentResult.status = paymentStatus;
      ride.setChargeId(farePayment.getChargeId());
      farePayment.setPaymentStatus(paymentStatus);
    } catch (RideAustinException | PaymentException e) {
      log.error("Payment failed", e);
      paymentStatus = handleError(ride, rider, card, freeCreditOverride, e);
      farePayment.setPaymentStatus(paymentStatus);
      paymentResult.success = false;
      paymentResult.status = paymentStatus;
    } finally {
      farePayment.setStripeCreditCharge(primaryRiderFare);
      if (fareDetails != null) {
        farePayment.setFreeCreditCharged(fareDetails.getFreeCreditCharged());
      }
      if (!tokenBasedPayment) {
        farePayment.setUsedCard(card);
      }
      farePaymentService.updateFarePayment(farePayment);
    }
    riderDslRepository.save(rider);
    return paymentResult;
  }

  @Override
  protected boolean shouldUseEstimate() {
    return true;
  }

  @Override
  protected void handleSuccess(Ride ride, FarePaymentService.FarePaymentInfo farePaymentInfo, PromocodeUseResult promocodeUseResult, PaymentResult paymentResult) throws RideAustinException {
    stripeService.refundPreCharge(ride);
    ride.setPaymentStatus(PaymentStatus.PREPAID_UPFRONT);
    unlockPayments(ride, paymentResult);
  }

  @Override
  protected PromocodeUseResult usePromocode(Ride ride) {
    final Optional<FareDetails> fareDetails = fareService.calculateTotalFare(ride, null, false, true);
    if (fareDetails.isPresent()) {
      final Optional<Campaign> campaign = campaignService.findExistingCampaignForRide(ride);
      return promocodeService.usePromocode(new PrevalidatedPromocodeUseRequest(ride, fareDetails.get(), campaign.isPresent()), true);
    }
    return null;
  }
}
