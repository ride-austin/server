package com.rideaustin.service.thirdparty;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.money.Money;
import org.joda.time.DateTime;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.enums.CardBrand;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.RiderCard;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.ForbiddenException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.rest.exception.UnAuthorizedException;
import com.stripe.Stripe;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.RateLimitException;
import com.stripe.exception.StripeException;
import com.stripe.model.Card;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import com.stripe.model.ExternalAccount;
import com.stripe.model.ExternalAccountCollection;
import com.stripe.model.Token;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Conditional(StripeImplCondition.class)
public class StripeServiceImpl implements StripeService {

  private static final int MAX_PAGE_LIMIT = 100;
  private static final String CREDIT_CARD_NOT_APPROVED_MESSAGE = "Sorry, your credit card was not approved";
  private static final String SOURCE_KEY = "source";

  private DateTimeFormatter defaultDescriptionDateFormat = DateTimeFormatter.ofPattern("YYYY-mm-dd HH:mm:ss z");
  private final StripeApprovalChecker approvalChecker;
  private final StripeFundingChecker fundingChecker;
  private final ConfigurationItemCache configurationItemCache;

  @Inject
  public StripeServiceImpl(Environment env, ConfigurationItemCache configurationItemCache) {
    Stripe.apiKey = env.getProperty("rideaustin.stripe.key");
    defaultDescriptionDateFormat.withZone(ZoneId.of("GMT"));
    this.approvalChecker = new StripeApprovalChecker(env);
    this.fundingChecker = new StripeFundingChecker(env);
    this.configurationItemCache = configurationItemCache;
  }

  public String createStripeAccount(Rider rider) throws RideAustinException {
    if (rider.getStripeId() != null) {
      return rider.getStripeId();
    }

    Map<String, Object> customerParams = ImmutableMap.of("email", rider.getUser().getEmail());
    Customer stripeCustomer = makeStripeServiceRequest(() -> Customer.create(customerParams));
    return stripeCustomer.getId();
  }

  public RiderCard createCardForRider(Rider rider, String cardToken) throws RideAustinException {
    if (rider == null) {
      throw new ForbiddenException("Rider not provided");
    }
    Customer customer = makeStripeServiceRequest(() -> Customer.retrieve(rider.getStripeId()));
    Map<String, Object> params = ImmutableMap.of(SOURCE_KEY, cardToken);
    Card card = (Card) makeStripeServiceRequest(() -> customer.getSources().create(params));

    RiderCard riderCard = new RiderCard();
    riderCard.setStripeCardId(card.getId());
    riderCard.setRider(rider);
    riderCard.setCardNumber(card.getLast4());
    riderCard.setCardExpired(isCardExpired(card.getExpYear(), card.getExpMonth()));
    riderCard.setFingerprint(card.getFingerprint());
    riderCard.setExpirationYear(String.valueOf(card.getExpYear()));
    riderCard.setExpirationMonth(String.valueOf(card.getExpMonth()));

    // "American Express" -> "AMERICAN_EXPRESS", "Diners Club" -> "DINERS_CLUB"
    String brand = card.getBrand().toUpperCase().replaceAll("\\s+", "_");
    riderCard.setCardBrand(CardBrand.valueOf(CardBrand.class, brand));

    return riderCard;
  }

  public List<RiderCard> listRiderCards(Rider rider, List<RiderCard> riderCardsInDB) throws RideAustinException {
    Map<String, Object> cardParams = ImmutableMap.of("limit", MAX_PAGE_LIMIT, "object", "card");
    Iterable<ExternalAccount> collection = makeStripeServiceRequest(() -> {
      Customer customer = Customer.retrieve(rider.getStripeId());
      if (customer != null) {
        ExternalAccountCollection sources = customer.getSources();
        if (sources != null) {
          ExternalAccountCollection list = sources.list(cardParams);
          if (list != null) {
            return list.autoPagingIterable();
          }
        }
      }
      return Collections.emptyList();
    });

    // update the expired field of all the cards
    for (ExternalAccount item : collection) {
      Card card = (Card) item;
      if (card != null) {
        riderCardsInDB.stream()
          .filter(x -> x.getStripeCardId().equals(card.getId()))
          .findAny()
          .ifPresent(rc -> {
            rc.setCardExpired(isCardExpired(card.getExpYear(), card.getExpMonth()));
            rc.setSyncDate(new Date());
            rc.setExpirationMonth(String.valueOf(card.getExpMonth()));
            rc.setExpirationYear(String.valueOf(card.getExpYear()));
          });
      }
    }
    return riderCardsInDB;
  }

  public void deleteCardForRider(RiderCard riderCard) throws RideAustinException {
    Customer customer = makeStripeServiceRequest(() -> Customer.retrieve(riderCard.getRider().getStripeId()));
    makeStripeServiceRequest(() -> customer.getSources().retrieve(riderCard.getStripeCardId()).delete());
  }

  private boolean isCardExpired(int expYear, int expMonth) {
    return new DateTime(expYear, expMonth, 1, 0, 0).plusMonths(1).withTimeAtStartOfDay().isBeforeNow();
  }

  public String receiveTokenPayment(Rider rider, Ride ride, Money chargeAmount) throws RideAustinException {
    return makeTokenPayment(rider, ride, chargeAmount, true);
  }

  @Override
  public String holdTokenPayment(Rider rider, Ride ride, Money chargeAmount) throws RideAustinException {
    return makeTokenPayment(rider, ride, chargeAmount, false);
  }

  private String makeTokenPayment(Rider rider, Ride ride, Money chargeAmount, final boolean capture) throws RideAustinException {
    long cents = chargeAmount.multipliedBy(100L).getAmount().longValue();
    log.info(String.format("[STRIPE][Ride #%d] Charging %s with AP", ride.getId(), chargeAmount.toString()));
    Charge charge = makeTokenPayment(rider, cents, capture, String.format("Ride %s payment by %s",
      ride.getId(), rider.getEmail()));
    return charge.getId();
  }

  public String receiveCardPayment(Rider rider, Ride ride, Rider cardRider, String stripeCardToken, Money chargeAmount) throws RideAustinException {
    return makeCardPayment(rider, ride, cardRider, stripeCardToken, chargeAmount, true);
  }

  @Override
  public String holdCardPayment(Rider rider, Ride ride, Rider cardRider, String stripeCardToken, Money chargeAmount) throws RideAustinException {
    return makeCardPayment(rider, ride, cardRider, stripeCardToken, chargeAmount, false);
  }

  private String makeCardPayment(Rider rider, Ride ride, Rider cardRider, String stripeCardToken, Money chargeAmount, final boolean capture) throws RideAustinException {
    if (!cardRider.equals(rider)) {
      throw new UnAuthorizedException("A rider could only pay with his/her own card.");
    }

    log.info(String.format("[STRIPE][Ride #%d] Charging %s with CC", ride.getId(), chargeAmount.toString()));
    long cents = chargeAmount.multipliedBy(100L).getAmount().longValue();
    Charge charge = makeCardPayment(rider, stripeCardToken, cents, capture, String.format("Ride %s payment by %s",
      ride.getId(), rider.getEmail()));
    return charge.getId();
  }

  public void updateCardFingerPrint(RiderCard riderCard) throws RideAustinException {
    Customer customer = makeStripeServiceRequest(() -> Customer.retrieve(riderCard.getRider().getStripeId()));
    Card card = (Card) makeStripeServiceRequest(() -> customer.getSources().retrieve(riderCard.getStripeCardId()));
    riderCard.setFingerprint(card.getFingerprint());
  }

  @Override
  public void updateCardExpiration(RiderCard riderCard) throws RideAustinException {
    Customer customer = makeStripeServiceRequest(() -> Customer.retrieve(riderCard.getRider().getStripeId()));
    makeStripeServiceRequest(() -> {
        if (customer != null) {
          ExternalAccountCollection cards = customer.getSources();
          if (cards != null) {
            ExternalAccount card = cards
              .retrieve(riderCard.getStripeCardId());
            if (card != null) {
              card.update(ImmutableMap.of(
                "exp_month", Integer.valueOf(riderCard.getExpirationMonth()),
                "exp_year", Integer.valueOf(riderCard.getExpirationYear()))
              );
            }
          }
        }
        return null;
      }
    );
  }

  public String authorizeRide(Ride ride, RiderCard card) throws RideAustinException {
    final Integer preAuth = configurationItemCache.getConfigAsInt(ClientType.CONSOLE, "stripe", "cardPreauth");
    Charge charge = makeCardPayment(ride.getRider(), card.getStripeCardId(), preAuth, false,
      String.format("Ride request by %s at %s", ride.getRider().getEmail(), defaultDescriptionDateFormat.format(ZonedDateTime.now())));
    if (approvalChecker.checkApproval(charge)) {
      return charge.getId();
    }
    throw new BadRequestException(CREDIT_CARD_NOT_APPROVED_MESSAGE);
  }

  @Override
  public String authorizeRide(Ride ride, String token) throws RideAustinException {
    final Integer preAuth = configurationItemCache.getConfigAsInt(ClientType.CONSOLE, "stripe", "applePayPreauth");
    log.info(String.format("[STRIPE][Ride %d] Authorizing with AP %s", ride.getId(), token));
    final Boolean fundingApproved = makeStripeServiceRequest(() -> {
      final Token retrievedToken = Token.retrieve(token);
      return fundingChecker.checkFunding(retrievedToken.getCard());
    });
    if (!fundingApproved) {
      throw new BadRequestException(CREDIT_CARD_NOT_APPROVED_MESSAGE);
    }
    makeStripeServiceRequest(() -> {
      Customer customer = Customer.retrieve(ride.getRider().getStripeId());
      if (customer != null) {
        Map<String, Object> params = ImmutableMap.of(SOURCE_KEY, token);
        ExternalAccount source = customer.getSources().create(params);
        customer.update(ImmutableMap.of("default_source", source.getId()));
      }
      return null;
    });
    Charge charge = makeTokenPayment(ride.getRider(), preAuth, false, String.format("Apple pay authorization for %s, ride #%d", ride.getRider().getEmail(), ride.getId()));
    if (approvalChecker.checkApproval(charge)) {
      return charge.getId();
    }
    throw new BadRequestException(CREDIT_CARD_NOT_APPROVED_MESSAGE);
  }

  public void refundPreCharge(@Nonnull Ride ride) throws RideAustinException {
    if (StringUtils.isNotBlank(ride.getPreChargeId())) {
      refundCharge(ride.getPreChargeId());
    }
  }

  @Override
  public String captureCharge(Ride ride, Money override) throws RideAustinException {
    if (StringUtils.isEmpty(ride.getChargeId())) {
      return null;
    }
    if (override == null) {
      return makeStripeServiceRequest(() -> {
        Charge charge = Charge.retrieve(ride.getChargeId());
        if (charge != null && BooleanUtils.isNotTrue(charge.getRefunded())) {
          log.info("Capturing charge: {}", ride.getChargeId());
          charge.capture();
        } else {
          log.warn("Invalid charge to capture: {}", ride.getChargeId());
        }
        return null;
      });
    } else {
      refundCharge(ride.getChargeId());
      final Rider rider = ride.getRider();
      if (StringUtils.isNotEmpty(ride.getApplePayToken())) {
        return receiveTokenPayment(rider, ride, override);
      } else {
        final RiderCard card = rider.getPrimaryCard();
        return receiveCardPayment(rider, ride, rider, card.getStripeCardId(), override);
      }
    }
  }

  @Override
  public void refundCharge(String chargeId) throws RideAustinException {
    makeStripeServiceRequest(() -> {
      Charge charge = Charge.retrieve(chargeId);
      if (charge != null && BooleanUtils.isNotTrue(charge.getRefunded())) {
        log.info("Refunding charge: {}", chargeId);
        charge.refund();
      } else {
        log.warn("Invalid charge to refund: {}", chargeId);
      }
      return null;
    });
  }

  @VisibleForTesting
  <T> T makeStripeServiceRequest(StripeRequest<T> func) throws RideAustinException {
    // https://stripe.com/docs/api/java#errors
    try {
      return makeStripeServiceRequestWithRetries(func);
    } catch (APIConnectionException e) {
      log.error("Network communication with Stripe failed.", e);
      throw new ServerError("Our service is temporarily unavailable, please try it again in few minutes.", e);
    } catch (RateLimitException e) {
      log.error("Too many requests made to the API too quickly.", e);
      throw new ServerError("Our service is temporarily unavailable, please try it again in few minutes.", e);
    } catch (CardException e) {
      log.error("Invalid card is passed in the request: " + e.getRequestId(), e);
      throw new BadRequestException(CREDIT_CARD_NOT_APPROVED_MESSAGE);
    } catch (InvalidRequestException e) {
      log.error("Invalid data is passed in the request: " + e.getRequestId(), e);
      throw new BadRequestException(e.getMessage());
    } catch (AuthenticationException e) {
      log.error("Stripe integration is wrongly configured/implemented for the request: " + e.getRequestId(), e);
      throw new ServerError(e);
    } catch (StripeException e) {
      // Display a very generic error to the user, and maybe send yourself an email
      log.error("Unexpected stripe exception in the request: " + e.getRequestId(), e);
      throw new ServerError(e);
    } catch (Exception e) {
      log.error("Unexpected exception in Stripe integration.", e);
      throw new ServerError(e);
    }
  }

  @VisibleForTesting
  <T> T makeStripeServiceRequestWithRetries(StripeRequest<T> func) throws AuthenticationException, InvalidRequestException,
    APIConnectionException, CardException, APIException {
    int maxAttempts = 3;
    for (int attempt = 0; attempt < maxAttempts; ++attempt) {
      try {
        return func.run();
      } catch (APIConnectionException e) {
        if (attempt < maxAttempts - 1) {
          log.info("Got temporary connection error from Stripe API. Retrying...");
          continue;
        }
        throw e;
      }
    }
    throw new IllegalStateException("Unexpected exception: the program should not reach here!!!");
  }

  @VisibleForTesting
  @FunctionalInterface
  interface StripeRequest<T> {
    T run() throws AuthenticationException, InvalidRequestException, APIConnectionException, CardException,
      APIException;
  }

  private Charge makeTokenPayment(Rider rider, long cents, boolean capture, String description) throws RideAustinException {
    Map<String, Object> chargeParams = ImmutableMap.<String, Object>builder()
      .put("amount", cents)
      .put("capture", capture)
      .put("currency", "usd")
      .put("description", description)
      .put("customer", rider.getStripeId())
      .build();
    final Charge charge = makeStripeServiceRequest(() -> Charge.create(chargeParams));
    if (capture) {
      makeStripeServiceRequest(() -> charge.getSource().delete());
    }
    return charge;
  }

  private Charge makeCardPayment(Rider rider, String stripeCardToken, long cents, boolean capture, String description) throws RideAustinException {
    Map<String, Object> chargeParams = ImmutableMap.<String, Object>builder()
      .put("amount", cents)
      .put("capture", capture)
      .put("currency", "usd")
      .put(SOURCE_KEY, stripeCardToken)
      .put("description", description)
      .put("customer", rider.getStripeId())
      .build();
    return makeStripeServiceRequest(() -> Charge.create(chargeParams));
  }

  private static class StripeApprovalChecker {

    private final Set<String> failedStatuses;
    private final Set<String> failedOutcomes;

    StripeApprovalChecker(Environment environment) {
      String failedStatusesList = environment.getProperty("stripe.failed.statuses");
      String failedOutcomesList = environment.getProperty("stripe.failed.outcomes");
      this.failedStatuses = Sets.newHashSet(failedStatusesList.split(","));
      this.failedOutcomes = Sets.newHashSet(failedOutcomesList.split(","));
    }

    boolean checkApproval(Charge charge) {
      return !(failedStatuses.contains(charge.getStatus()) || failedOutcomes.contains(charge.getOutcome().getNetworkStatus()));
    }
  }

  private static class StripeFundingChecker {

    private final Set<String> eligibleFundings;

    StripeFundingChecker(Environment environment) {
      String eligibleFundingsList = environment.getProperty("stripe.eligible.fundings");
      this.eligibleFundings = Sets.newHashSet(eligibleFundingsList.split(","));
    }

    boolean checkFunding(Card card) {
      log.info(String.format("[STRIPE][APPREAUTH] Checking funding for card %s: card is %s. Approved fundings are: %s", card.getLast4(),
        card.getFunding(), String.join(",", eligibleFundings)));
      return eligibleFundings.contains(card.getFunding());
    }
  }
}
