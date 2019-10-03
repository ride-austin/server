package com.rideaustin.service.payment;

import static com.rideaustin.utils.SafeZeroUtils.safeZero;

import java.util.Date;
import java.util.Optional;

import javax.persistence.LockTimeoutException;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.LockAcquisitionException;
import org.joda.money.Money;
import org.joda.money.MoneyUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.Constants;
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
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.CampaignService;
import com.rideaustin.service.CurrentSessionService;
import com.rideaustin.service.FareService;
import com.rideaustin.service.RiderCardService;
import com.rideaustin.service.config.RidePaymentConfig;
import com.rideaustin.service.farepayment.FarePaymentService;
import com.rideaustin.service.farepayment.FarePaymentService.FarePaymentInfo;
import com.rideaustin.service.notifications.PushNotificationsFacade;
import com.rideaustin.service.promocodes.PromocodeService;
import com.rideaustin.service.promocodes.PromocodeUseRequest;
import com.rideaustin.service.promocodes.PromocodeUseResult;
import com.rideaustin.service.ride.RideLoadService;
import com.rideaustin.service.thirdparty.StripeService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@Profile("!itest")
public class PaymentService extends AbstractPaymentService {

  private static final String ERROR_CHARGING_RIDE_MESSAGE = "Error charging ride #%d. Error message: %s";

  protected final RiderDslRepository riderDslRepository;
  protected final FareService fareService;
  protected final RiderCardService riderCardService;
  private final RidePaymentConfig config;
  private final RideLoadService rideLoadService;

  public PaymentService(CurrentSessionService currentSessionService, StripeService stripeService, RideDslRepository rideDslRepository,
    PaymentEmailService paymentEmailService, RiderDslRepository riderDslRepository, PromocodeService promocodeService,
    FarePaymentService farePaymentService, FareService fareService, RiderCardService riderCardService,
    RideLoadService rideLoadService, PushNotificationsFacade pushNotificationsFacade, CampaignService campaignService,
    RidePaymentConfig config) {
    super(currentSessionService, stripeService, rideDslRepository, paymentEmailService, promocodeService, farePaymentService,
      pushNotificationsFacade, campaignService);
    this.riderDslRepository = riderDslRepository;
    this.fareService = fareService;
    this.riderCardService = riderCardService;
    this.rideLoadService = rideLoadService;
    this.config = config;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = {LockTimeoutException.class, LockAcquisitionException.class, CannotAcquireLockException.class})
  public boolean processRidePayment(Ride ride) throws RideAustinException {
    return processRidePayment(ride.getId());
  }

  public boolean processRidePayment(Long rideId) throws RideAustinException {
    return processRidePayment(rideId, null);
  }

  public boolean processRidePayment(long rideId, String applePayToken) throws RideAustinException {
    PaymentResult paymentResult = new PaymentResult();
    Ride ride;
    try {
      ride = rideLoadService.findOneForUpdateWithRetry(rideId);
      if (ride == null) {
        throw new NotFoundException(String.format("Ride not found or already processing. Try again later. ID: %d", rideId));
      } else if (!ride.getRider().getUser().isEnabled()) {
        log.error(String.format("User %s has been disabled, skipping payment processing", ride.getRider().getUser().getEmail()));
        return false;
      }
      if (PaymentStatus.PAID.equals(ride.getPaymentStatus())) {
        log.info("Ride is already charged");
        return false;
      } else if (PaymentStatus.PREPAID_UPFRONT.equals(ride.getPaymentStatus())) {
        return postProcessPayment(ride);
      }

      FarePayment farePaymentForMainRider = farePaymentService.createFarePaymentForMainRider(ride);

      PromocodeUseResult promocodeUseResult = usePromocode(ride);

      FarePaymentInfo farePaymentInfo = farePaymentService.createFarePaymentInfo(ride, promocodeUseResult, shouldUseEstimate());
      Money primaryRiderFare = performParticipantPayments(farePaymentInfo, ride);
      paymentResult = performPrimaryPayment(farePaymentInfo.getPrimaryRiderFarePayment(), ride, primaryRiderFare, promocodeUseResult, applePayToken);

      if (paymentResult.success) {
        farePaymentForMainRider.setProvider(paymentResult.method);
        handleSuccess(ride, farePaymentInfo, promocodeUseResult, paymentResult);
      } else {
        handleFailure(ride, farePaymentInfo, paymentResult);
      }

      riderDslRepository.save(ride.getRider());
      rideDslRepository.save(ride);
      farePaymentService.updateFarePayment(farePaymentForMainRider);
    } catch (LockAcquisitionException | LockTimeoutException e) {
      log.warn("Ride payment is already being processed. Ride id:{}", rideId, e);
    }
    return paymentResult.success;
  }

  protected PromocodeUseResult usePromocode(Ride ride) {
    final Optional<Campaign> campaign = campaignService.findExistingCampaignForRide(ride);
    PromocodeUseResult promocodeUseResult = promocodeService.usePromocode(new PromocodeUseRequest(ride, campaign.isPresent()));
    if (promocodeUseResult.isSuccess()) {
      ride.setPromocodeRedemptionId(promocodeUseResult.getAffectedPromocodeRedemption().getId());
    }
    return promocodeUseResult;
  }

  protected boolean shouldUseEstimate() {
    return false;
  }

  private boolean postProcessPayment(Ride ride) throws RideAustinException {
    final FareDetails upfrontFareDetails = ride.getFareDetails();
    ride.setFareDetails(new FareDetails());
    final Optional<FareDetails> totalFare = fareService.calculateTotalFare(ride, null);
    if (!totalFare.isPresent()) {
      log.error("Failed to assess ride");
      return false;
    }
    final FarePayment farePayment = farePaymentService.getFarePaymentForRide(ride);
    boolean result;
    PromocodeUseResult promocodeUseResult = null;
    try {
      Money override = null;
      final Money finalRideCost = totalFare.get().getRideCost();
      final Money upfrontRideCost = upfrontFareDetails.getRideCost();
      if (finalRideCost.minus(upfrontRideCost).isGreaterThan(config.getUpfrontRecalculationThreshold())) {
        ride.setFareDetails(totalFare.get());
        promocodeUseResult = usePromocode(ride);
        ride.setFareDetails(upfrontFareDetails);
        final Optional<FareDetails> finalFare = fareService.calculateFinalFare(ride, promocodeUseResult);
        override = finalFare.map(FareDetails::getStripeCreditCharge).orElse(null);
        log.info(String.format("[PAYMENT][Ride %d] Overriding upfront price with %s", ride.getId(), override));
        finalFare.ifPresent(ride::setFareDetails);
      } else {
        ride.setFareDetails(upfrontFareDetails);
        promocodeUseResult = usePromocode(ride);
        final Optional<FareDetails> finalFare = fareService.calculateFinalFare(ride, promocodeUseResult, true);
        if (!finalFare.isPresent()) {
          log.error("Failed to assess ride");
          return false;
        }
        final Money tip = finalFare.get().getTip();
        if (MoneyUtils.isPositive(tip)) {
          override = finalFare.get().getStripeCreditCharge();
          log.info(String.format("[PAYMENT][Ride %d] Overriding upfront price for tip of %s with %s", ride.getId(), tip, override));
        }
      }
      if (override != null) {
        farePayment.setStripeCreditCharge(override);
      }
      farePayment.setFreeCreditCharged(ride.getFreeCreditCharged());
      final String capturedCharge = stripeService.captureCharge(ride, override);
      ride.setChargeId(capturedCharge);
      farePayment.setChargeId(capturedCharge);
      farePayment.setPaymentStatus(PaymentStatus.PAID);
      final FarePaymentInfo farePaymentInfo = farePaymentService.createFarePaymentInfo(ride, promocodeUseResult, shouldUseEstimate());
      handleSuccess(ride, farePaymentInfo, promocodeUseResult,
        new PaymentResult(true, farePayment.getProvider(), PaymentStatus.PAID));
      result = true;
    } catch (RideAustinException e) {
      log.error("Exception occured while payment", e);
      final FarePaymentInfo farePaymentInfo = farePaymentService.createFarePaymentInfo(ride, promocodeUseResult, shouldUseEstimate());
      handleFailure(ride, farePaymentInfo,
        new PaymentResult(false, farePayment.getProvider(), PaymentStatus.UNPAID));
      result = false;
    } finally {
      farePaymentService.updateFarePayment(farePayment);
      rideDslRepository.save(ride);
    }
    return result;
  }

  protected void lockPayments(Ride ride, PaymentResult paymentResult) throws RideAustinException {
    if (paymentResult.method == PaymentProvider.CREDIT_CARD) {
      riderCardService.lockCard(ride.getRider().getPrimaryCard(), ride);
      paymentEmailService.sendLockCardEmail(ride);
    }
  }

  protected void unlockPayments(Ride ride, PaymentResult paymentResult) throws RideAustinException {
    if (paymentResult.method == PaymentProvider.CREDIT_CARD && riderCardService.checkIfCardLocked(ride.getRider().getPrimaryCard())) {
      paymentEmailService.sendUnlockCardEmail(ride);
    }
    riderCardService.unlockCard(ride);
  }

  protected PaymentResult performPrimaryPayment(FarePayment farePayment, Ride ride, Money primaryRiderFare, PromocodeUseResult freeCreditOverride, String applePayToken) throws RideAustinException {
    Rider rider = farePayment.getRider();
    String effectiveApplePayToken = Optional.ofNullable(applePayToken).orElse(ride.getApplePayToken());
    PaymentResult paymentResult = new PaymentResult(effectiveApplePayToken);
    RiderCard card = rider.getPrimaryCard();
    riderCardService.updateCardFingerprint(card);
    Optional<FareDetails> detailsOptional = fareService.calculateFinalFare(ride, freeCreditOverride,ride.getPaymentStatus() == PaymentStatus.PREPAID_UPFRONT);
    FareDetails fareDetails = detailsOptional.orElse(null);
    boolean tokenBasedPayment = StringUtils.isNotBlank(effectiveApplePayToken);
    PaymentStatus paymentStatus = PaymentStatus.PAID;
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
            farePayment.setChargeId(stripeService.receiveTokenPayment(rider, ride, primaryRiderFare));
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
          farePayment.setChargeId(stripeService.receiveCardPayment(rider, ride, card.getRider(), card.getStripeCardId(), primaryRiderFare));
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

  protected FareDetails handleApplePayFailure(FarePayment farePayment, Ride ride, PromocodeUseResult freeCreditOverride, Rider rider, RiderCard card) throws PaymentException, RideAustinException {
    Optional<FareDetails> detailsOptional = fareService.calculateTotalFare(ride, freeCreditOverride, true);
    if (!detailsOptional.isPresent()) {
      throw new PaymentException("Ride can't be assessed");
    }
    FareDetails newFareDetails = detailsOptional.get();
    newFareDetails.setRoundUpAmount(Constants.ZERO_USD);
    newFareDetails.setTip(ride.getTip());
    newFareDetails.setDriverPayment(newFareDetails.getDriverPayment().plus(safeZero(ride.getTip())));
    ride.setFareDetails(newFareDetails);
    ride.setCharity(null);
    log.info("Processing card payment for ride #" + ride.getId());
    farePayment.setChargeId(stripeService.receiveCardPayment(rider, ride, card.getRider(), card.getStripeCardId(), newFareDetails.getTotalCharge()));
    return newFareDetails;
  }

  protected boolean validateCard(Rider rider, RiderCard card, Ride ride) {
    if (card == null) {
      if (rider.getId() == ride.getRider().getId()) {
        log.error(String.format(ERROR_CHARGING_RIDE_MESSAGE, ride.getId(), "No primary card for main rider"));
      } else {
        log.error(String.format(ERROR_CHARGING_RIDE_MESSAGE, ride.getId(), "No primary card for ride participant"));
      }
      return false;
    } else {
      return card.isChargeable();
    }
  }

  protected PaymentStatus handleError(Ride ride, Rider rider, RiderCard card, PromocodeUseResult freeCreditOverride, Exception e) throws RideAustinException {
    boolean isCancelledRide = ride.isUserCancelled();
    Optional<FareDetails> fareDetails;
    ride.getFareDetails().setStripeCreditCharge(Constants.ZERO_USD);
    if (isCancelledRide) {
      fareDetails = fareService.processCancellation(ride, true);
    } else {
      fareDetails = fareService.calculateTotalFare(ride, freeCreditOverride);
      fareDetails.ifPresent(fd -> {
        fd.setTip(ride.getTip());
        fd.setDriverPayment(fd.getDriverPayment().plus(safeZero(ride.getTip())));
      });
    }
    fareDetails.ifPresent(ride::setFareDetails);
    if (rider.getId() == ride.getRider().getId()) {
      log.error(String.format(ERROR_CHARGING_RIDE_MESSAGE, ride.getId(), e.getMessage()));
      paymentEmailService.notifyRiderInvalidPayment(ride, rider, card);
      log.error("Unable to charge main rider card", e);
    } else {
      log.error("Unable to charge ride participant card", e);
    }
    PaymentStatus paymentStatus = PaymentStatus.UNPAID;
    if (card != null) {
      card.increaseFailedAttempts();
      if (card.isFailedChargeThresholdExceeded()) {
        paymentStatus = PaymentStatus.BLOCKED;
      }
      card.setLastFailureDate(new Date());
      riderCardService.updateRiderCard(card);
    }
    rideDslRepository.save(ride);
    return paymentStatus;
  }

  private Money performParticipantPayments(FarePaymentInfo farePaymentInfo, Ride ride) throws RideAustinException {
    boolean success;
    Money primaryRiderFare = farePaymentInfo.getPrimaryRiderFare();
    for (FarePayment fp : farePaymentInfo.getSecondaryRiderPayments()) {
      if (isFarePaymentAlreadyCharged(fp)) {
        continue;
      }
      Money farePerParticipants = farePaymentInfo.getFarePerParticipants();
      success = performParticipantPayment(fp, ride, farePerParticipants);
      if (!success) {
        primaryRiderFare = primaryRiderFare.plus(farePerParticipants);
      } else {
        paymentEmailService.sendEndRideParticipantEmail(ride, fp);
      }
    }
    return primaryRiderFare;
  }

  private boolean performParticipantPayment(FarePayment farePayment, Ride ride, Money paymentAmount) throws RideAustinException {
    Rider rider = farePayment.getRider();
    boolean success;
    RiderCard card = rider.getPrimaryCard();
    riderCardService.updateCardFingerprint(card);
    if (!validateCard(rider, card, ride)) {
      return false;
    }
    try {
      if (!MoneyUtils.isZero(paymentAmount)) {
        farePayment.setChargeId(stripeService.receiveCardPayment(rider, ride, card.getRider(), card.getStripeCardId(), paymentAmount));
      }
      farePayment.setStripeCreditCharge(paymentAmount);
      farePayment.setFreeCreditCharged(Constants.ZERO_USD);
      farePayment.setUsedCard(card);
      farePayment.setPaymentStatus(PaymentStatus.PAID);
      farePayment.setProvider(PaymentProvider.CREDIT_CARD);
      success = true;
    } catch (RideAustinException e) {
      success = false;
      PaymentStatus paymentStatus = handleError(ride, rider, card, null, e);
      farePayment.setPaymentStatus(paymentStatus);
    }
    farePaymentService.updateFarePayment(farePayment);
    riderDslRepository.save(rider);
    return success;
  }

  private boolean isFarePaymentAlreadyCharged(FarePayment farePayment) {
    return StringUtils.isNotEmpty(farePayment.getChargeId());
  }

}
