package com.rideaustin.service;

import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.enums.PaymentStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.RiderCard;
import com.rideaustin.model.user.RiderCardLock;
import com.rideaustin.repo.dsl.RiderCardDslRepository;
import com.rideaustin.repo.dsl.RiderCardLockDslRepository;
import com.rideaustin.repo.dsl.RiderDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.model.UpdateCardDto;
import com.rideaustin.service.thirdparty.StripeService;
import com.rideaustin.service.user.RiderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RiderCardService {

  private final StripeService stripeService;
  private final RiderService riderService;

  private final RiderCardDslRepository riderCardDslRepository;
  private final RiderCardLockDslRepository riderCardLockDslRepository;
  private final RiderDslRepository riderDslRepository;

  public void checkIfCardLockedWithFail(RiderCard riderCard) throws BadRequestException {
    if (checkIfCardLocked(riderCard)) {
      throw new BadRequestException("Your credit card has been locked by your credit card company - please contact support@example.com");
    }
  }

  public boolean checkIfCardLocked(RiderCard riderCard) {
    if (riderCard.getFingerprint() != null) {
      RiderCardLock lock = riderCardLockDslRepository.findByFingerprint(riderCard.getFingerprint());
      return lock != null || riderCard.isFailedChargeThresholdExceeded();
    }
    return riderCard.isFailedChargeThresholdExceeded();
  }

  public List<RiderCard> listRiderCards(long riderId) throws RideAustinException {
    Rider rider = riderService.findRider(riderId);
    if (StringUtils.isBlank(rider.getStripeId())) {
      return Collections.emptyList();
    }
    List<RiderCard> cards = riderCardDslRepository.findByRider(rider);
    if (cards.isEmpty()) {
      return Collections.emptyList();
    }

    // update the "expired" field if last sync was later than 24h ago
    Optional<Date> lastSyncDate = cards.stream()
      .map(RiderCard::getSyncDate)
      .filter(Objects::nonNull)
      .min(comparing(identity()));
    if (!lastSyncDate.isPresent() || lastSyncDate.get().before(DateUtils.addDays(new Date(), -1))) {
      cards = stripeService.listRiderCards(rider, cards);
    }
    riderCardDslRepository.saveMany(cards);
    cards.forEach(c -> c.setPrimary(c.equals(rider.getPrimaryCard())));
    return cards;
  }

  public void deleteRiderCard(long riderId, long cardId) throws RideAustinException {
    Rider rider = riderService.findRider(riderId);
    checkActiveRide(rider);
    RiderCard card = getRiderCard(cardId, rider);
    checkIfCardLockedWithFail(card);
    if (card.equals(rider.getPrimaryCard())) {
      throw new BadRequestException("You cannot delete your primary credit card");
    }
    stripeService.deleteCardForRider(card);
    card.setRemoved(true);
    riderCardDslRepository.save(card);
  }

  public void updateRiderCard(long riderId, UpdateCardDto updateCardDto)
    throws RideAustinException {
    Rider rider = riderService.findRider(riderId);
    checkActiveRide(rider);
    RiderCard riderCard = getRiderCard(updateCardDto.getId(), rider);
    if (!riderCard.isRemoved()) {
      checkIfCardLockedWithFail(riderCard);
      if (updateCardDto.isPrimary()) {
        rider.setPrimaryCard(riderCard);
      }
      if (updateCardDto.getExpMonth() != null && updateCardDto.getExpYear() != null) {
        riderCard.setExpirationMonth(updateCardDto.getExpMonth());
        riderCard.setExpirationYear(updateCardDto.getExpYear());
        riderCard.setSyncDate(DateUtils.addDays(new Date(), -2));
        stripeService.updateCardExpiration(riderCard);
      }
      riderDslRepository.save(rider);
    }
  }

  public void updateRiderCard(RiderCard card) {
    riderCardDslRepository.save(card);
  }

  public RiderCard addRiderCard(long riderId, String cardToken) throws RideAustinException {
    Rider rider = riderService.findRider(riderId);
    checkActiveRide(rider);
    if (StringUtils.isBlank(rider.getStripeId())) {
      String stripeCustomerId = stripeService.createStripeAccount(rider);
      rider.setStripeId(stripeCustomerId);
      riderDslRepository.save(rider);
    }

    RiderCard riderCard = stripeService.createCardForRider(rider, cardToken);
    if (checkIfCardLocked(riderCard)) {
      stripeService.deleteCardForRider(riderCard);
      throw new BadRequestException("Your credit card has been locked by your credit card company - please contact support@example.com");
    }
    RiderCard savedCard = riderCardDslRepository.save(riderCard);
    if (rider.getPrimaryCard() == null) {
      rider.setPrimaryCard(savedCard);
      savedCard.setPrimary(true);
      riderDslRepository.save(rider);
    }
    return savedCard;
  }

  public void updateCardFingerprint(RiderCard card) {
    if (card != null && card.getFingerprint() == null && !card.isRemoved()) {
      try {
        stripeService.updateCardFingerPrint(card);
        riderCardDslRepository.save(card);
      } catch (RideAustinException e) {
        log.error("Unable to update card fingerprint", e);
      }
    }
  }

  private void checkActiveRide(@Nonnull Rider rider) throws BadRequestException {
    Ride ride = riderDslRepository.getActiveRide(rider);
    if (ride != null) {
      throw new BadRequestException("Cards changes are not allowed while on a ride");
    }
  }

  @Nonnull
  private RiderCard getRiderCard(long cardId, @Nonnull Rider rider) throws NotFoundException {
    RiderCard card = riderCardDslRepository.findOne(cardId);
    if (card == null || !rider.equals(card.getRider())) {
      throw new NotFoundException("Invalid card id: " + cardId);
    }
    if(card.isRemoved()){
      throw new NotFoundException("Card is removed");
    }
    return card;
  }

  public void unlockCard(Ride ride) {
    List<RiderCardLock> locks = riderCardLockDslRepository.findByRide(ride);
    locks.forEach(riderCardLockDslRepository::delete);
  }

  public void lockCard(RiderCard card, Ride ride) {
    if (card.getFingerprint() != null) {
      RiderCardLock lock = riderCardLockDslRepository.findByFingerprintAndRide(card.getFingerprint(), ride);
      if (lock == null) {
        RiderCardLock rideCardLock = new RiderCardLock();
        rideCardLock.setCardFingerprint(card.getFingerprint());
        rideCardLock.setRide(ride);
        riderCardLockDslRepository.save(rideCardLock);
      }
    } else {
      log.error("Unable to lock card: " + card.getId());
    }
  }

  public void unlock(long id) {
    Rider rider = riderDslRepository.getRider(id);
    rider.setPaymentStatus(PaymentStatus.PAID);
    riderDslRepository.save(rider);

    List<String> fingerprints = riderCardDslRepository.findFingerprintsByRider(rider);
    List<RiderCardLock> locks = riderCardLockDslRepository.findByFingerprints(fingerprints);
    locks.forEach(riderCardLockDslRepository::delete);
    List<RiderCard> cards = riderCardDslRepository.findByRider(rider);
    cards.forEach(RiderCard::resetFailureCount);
    riderCardDslRepository.saveMany(cards);
  }

}
