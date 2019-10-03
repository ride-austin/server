package com.rideaustin.service.thirdparty;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.apache.commons.lang3.RandomStringUtils;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;
import com.rideaustin.model.enums.CardBrand;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.RiderCard;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.test.common.Sleeper;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Conditional(StripeMockCondition.class)
public class StripeServiceMockImpl implements StripeService {

  @Setter
  private boolean failOnCardCharge = false;
  @Setter
  private boolean failOnApplePayCharge = false;
  @Setter
  private Rider failedRider;
  @Getter
  private Money applePayCharged = Money.zero(CurrencyUnit.USD);
  @Getter
  private Money cardCharged = Money.zero(CurrencyUnit.USD);
  @Setter
  private long preauthTimeout = 0;
  @Getter
  private boolean preauthCompleted = false;
  @Getter
  private boolean preauthRefunded = false;

  private final Sleeper sleeper;

  @Inject
  public StripeServiceMockImpl(Sleeper sleeper) {
    this.sleeper = sleeper;
  }

  public String createStripeAccount(Rider rider) {
    log.info("Mocked logic for createStripeAccount executed");
    return RandomStringUtils.randomAlphanumeric(10);
  }

  public RiderCard createCardForRider(Rider rider, String cardToken) {
    log.info("Mocked logic for StripeServiceMockImpl executed");
    RiderCard riderCard = new RiderCard();
    riderCard.setStripeCardId(cardToken);
    riderCard.setRider(rider);
    riderCard.setCardNumber(RandomStringUtils.randomNumeric(4));
    riderCard.setCardExpired(false);
    riderCard.setFingerprint(RandomStringUtils.randomAlphanumeric(5));

    riderCard.setCardBrand(CardBrand.VISA);

    return riderCard;
  }

  public List<RiderCard> listRiderCards(Rider rider, List<RiderCard> riderCardsInDB) {
    log.info("Mocked logic for listRiderCards executed");
    return ImmutableList.of(createCardForRider(rider, RandomStringUtils.randomAlphanumeric(5)),
      createCardForRider(rider, RandomStringUtils.randomAlphanumeric(5)),
      createCardForRider(rider, RandomStringUtils.randomAlphanumeric(5)));
  }

  public void deleteCardForRider(RiderCard riderCard) {
    log.info("Mocked logic for deleteCardForRider executed");
  }

  @Override
  public String receiveTokenPayment(Rider rider, Ride ride, Money chargeAmount) throws RideAustinException {
    log.info("Mocked logic for receivePayment executed");
    if (failOnApplePayCharge) {
      throw new ServerError("Mocked Exception for apple pay payment");
    }
    applePayCharged = chargeAmount;
    return RandomStringUtils.randomNumeric(8);
  }

  @Override
  public String holdTokenPayment(Rider rider, Ride ride, Money chargeAmount) throws RideAustinException {
    return receiveTokenPayment(rider, ride, chargeAmount);
  }

  @Override
  public String receiveCardPayment(Rider rider, Ride ride, Rider cardRider, String stripeCardToken, Money chargeAmount) throws RideAustinException {
    log.info("Mocked logic for receivePayment executed");
    if (failOnCardCharge || rider.equals(failedRider)) {
      throw new ServerError("Mocked Exception of payment");
    }
    cardCharged = chargeAmount;
    return RandomStringUtils.randomNumeric(8);
  }

  @Override
  public String holdCardPayment(Rider rider, Ride ride, Rider cardRider, String stripeCardToken, Money chargeAmount) throws RideAustinException {
    return receiveCardPayment(rider, ride, cardRider, stripeCardToken, chargeAmount);
  }

  public void updateCardFingerPrint(RiderCard riderCard) {
    log.info("Mocked logic for updateCardFingerPrint executed");
  }

  @Override
  public void updateCardExpiration(RiderCard riderCard) {
    log.info("Mocked logic for updateCardExpiration executed");
  }

  public String authorizeRide(Ride ride, RiderCard card) throws RideAustinException {
    if (failOnCardCharge || failOnApplePayCharge) {
      throw new ServerError("Mocked Exception of payment");
    }
    if (preauthTimeout > 0) {
      sleeper.sleep(preauthTimeout);
    }
    log.info("Mocked logic for authorizeRide executed");
    preauthCompleted = true;
    return RandomStringUtils.randomNumeric(8);
  }

  @Override
  public String authorizeRide(Ride ride, String token) {
    log.info("Mocked logic for authorizeRide executed");
    if (preauthTimeout > 0) {
      sleeper.sleep(preauthTimeout);
    }
    preauthCompleted = true;
    return RandomStringUtils.randomNumeric(5);
  }

  public void refundPreCharge(@Nonnull Ride ride) {
    log.info("Mocked logic for refundPreCharge executed");
    if (ride.getPreChargeId() != null) {
      preauthRefunded = true;
    }
  }

  @Override
  public String captureCharge(Ride ride, Money override) {
    log.info("Mocked logic for captureCharge executed");
    return null;
  }

  @Override
  public void refundCharge(String chargeId) {
    log.info("Mocked logic for refundCharge executed");
  }

  public void resetFlags() {
    this.failOnApplePayCharge = false;
    this.failOnCardCharge = false;
    this.cardCharged = Money.zero(CurrencyUnit.USD);
    this.applePayCharged = Money.zero(CurrencyUnit.USD);
    this.failedRider = null;
    this.preauthTimeout = 0;
    this.preauthRefunded = false;
    this.preauthCompleted = false;
  }

}
