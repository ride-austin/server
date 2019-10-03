package com.rideaustin.service.farepayment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.enums.SplitFareStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.splitfare.FarePayment;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.FarePaymentDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.repo.dsl.RiderDslRepository;
import com.rideaustin.rest.exception.ForbiddenException;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.SplitFareDto;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.notifications.PushNotificationsFacade;
import com.rideaustin.utils.PhoneNumberUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SplitFareService {

  private final FarePaymentDslRepository farePaymentDslRepository;
  private final RiderDslRepository riderDslRepository;
  private final RideDslRepository rideDslRepository;
  private final CurrentUserService currentUserService;
  private final PushNotificationsFacade pushNotificationsFacade;

  @Transactional
  public FarePayment sendSplitFareRequest(@Nonnull Long rideId, @Nonnull List<String> phoneNumbers) throws RideAustinException {
    Ride ride = rideDslRepository.findOne(rideId);
    User currentUser = currentUserService.getUser();

    checkIsAdminOrPartOfRide(ride, currentUser, true);

    Rider targetedRider = findByPhoneNumbers(phoneNumbers);

    if (targetedRider == null) {
      throw new NotFoundException("Rider not found");
    }
    if (!targetedRider.isActive() || targetedRider.getPrimaryCard() == null || targetedRider.getPrimaryCard().isCardExpired()) {
      throw new ForbiddenException("Rider is not yet activated");
    }

    if (targetedRider.getId() == ride.getRider().getId()) {
      throw new ForbiddenException("Rider is not allowed to share with himself");
    }

    FarePayment alreadyRequested = farePaymentDslRepository.findFarePayment(rideId, targetedRider.getId());
    if (alreadyRequested != null) {
      if (SplitFareStatus.DECLINED.equals(alreadyRequested.getSplitStatus())) {
        alreadyRequested.setSplitStatus(SplitFareStatus.REQUESTED);
        alreadyRequested.setUpdatedDate(new Date());
        pushNotificationsFacade.pushSplitFareRequestToRider(rideId, alreadyRequested.getId(), currentUser, targetedRider.getUser());
        return alreadyRequested;
      } else if (SplitFareStatus.REQUESTED.equals(alreadyRequested.getSplitStatus())) {
        throw new ForbiddenException("Rider is already requested for splitting fare on that ride");
      } else if (SplitFareStatus.ACCEPTED.equals(alreadyRequested.getSplitStatus())) {
        throw new ForbiddenException("Rider already accepted splitting fare on that ride");
      }
    }

    FarePayment farePayment = FarePayment.builder()
      .ride(ride)
      .rider(targetedRider)
      .splitStatus(SplitFareStatus.REQUESTED)
      .build();
    farePayment = farePaymentDslRepository.save(farePayment);
    pushNotificationsFacade.pushSplitFareRequestToRider(rideId, farePayment.getId(), currentUser, targetedRider.getUser());
    return farePayment;
  }

  @Transactional
  public FarePayment changeAcceptanceStatus(Long splitFareId, boolean acceptanceStatus) throws RideAustinException {
    User currentUser = currentUserService.getUser();
    FarePayment farePayment = farePaymentDslRepository.findOne(splitFareId);
    if (farePayment == null) {
      throw new NotFoundException("Split fare does not exist");
    }
    boolean isNotAdminOrTargetToSplit = !currentUser.isAdmin() && !isRiderSplitFare(farePayment, currentUser);
    if (isNotAdminOrTargetToSplit) {
      throw new ForbiddenException("You are not authorized to change acceptance status");
    }
    if (RideStatus.TERMINAL_STATUSES.contains(farePayment.getRide().getStatus())) {
      throw new ForbiddenException("Ride is already finished");
    }

    if (acceptanceStatus) {
      farePayment.setSplitStatus(SplitFareStatus.ACCEPTED);
    } else {
      farePayment.setSplitStatus(SplitFareStatus.DECLINED);
    }

    farePaymentDslRepository.save(farePayment);
    pushNotificationsFacade.pushSplitFareAcceptanceToRider(farePayment.getRide().getId(), farePayment.getId(), farePayment.getRide().getRider().getUser(), farePayment.getRider().getUser(), acceptanceStatus);
    return farePayment;
  }

  public List<SplitFareDto> getListForRide(Long rideId) throws RideAustinException {
    Ride ride = rideDslRepository.findOneWithRider(rideId);
    User currentUser = currentUserService.getUser();
    checkIsAdminOrPartOfRide(ride, currentUser, false);
    return farePaymentDslRepository.findFarePayments(rideId);
  }

  public List<SplitFareDto> findPendingSplitFareRequestForRider(Long riderId) throws RideAustinException {
    Rider rider = riderDslRepository.getRider(riderId);
    User currentUser = currentUserService.getUser();
    if (rider == null || currentUser == null) {
      throw new NotFoundException("No rider or current user");
    }
    if (!currentUser.isAdmin() && (currentUser.getId() != rider.getUser().getId())) {
      throw new ForbiddenException("Requested user not equal to current users");
    }
    return farePaymentDslRepository.findPendingSplitFareRequestForRider(riderId);
  }

  @Transactional
  public void removeRiderFromSplitting(Long splitFareId) throws RideAustinException {
    User currentUser = currentUserService.getUser();
    FarePayment farePayment = farePaymentDslRepository.findOne(splitFareId);
    if (farePayment == null) {
      throw new NotFoundException("Split fare does not exist");
    }
    checkIsAdminOrPartOfRide(farePayment.getRide(), currentUser, true);

    farePaymentDslRepository.delete(farePayment);
  }

  private Rider findByPhoneNumbers(List<String> providedPhoneNumbers) throws NotFoundException {

    List<Rider> listOfFoundRiders = new ArrayList<>();
    for (String original : providedPhoneNumbers) {
      String cleanedPhoneNumber = PhoneNumberUtils.cleanPhoneNumber(original);
      String last10NumberPhoneNumber = PhoneNumberUtils.onlyLast10Numbers(cleanedPhoneNumber);
      String bracketStandardPhoneNumber = PhoneNumberUtils.toBracketsStandard(cleanedPhoneNumber);

      Rider r = riderDslRepository.findByPhoneNumber(original, false);
      if (r != null) {
        listOfFoundRiders.add(r);
        continue;
      }
      if (cleanedPhoneNumber != null) {
        r = riderDslRepository.findByPhoneNumber(cleanedPhoneNumber, false);
        if (r != null) {
          listOfFoundRiders.add(r);
          continue;
        }
      }
      if (last10NumberPhoneNumber != null) {
        r = riderDslRepository.findByPhoneNumber(last10NumberPhoneNumber, true);
        if (r != null) {
          listOfFoundRiders.add(r);
          continue;
        }
      }
      if (bracketStandardPhoneNumber != null) {
        r = riderDslRepository.findByPhoneNumber(bracketStandardPhoneNumber, false);
        if (r != null) {
          listOfFoundRiders.add(r);
        }
      }
    }
    if (listOfFoundRiders.isEmpty()) {
      throw new NotFoundException("There is no rider registered with that phone number");
    }
    if (listOfFoundRiders.size() > 1) {
      if (providedPhoneNumbers.size() == 1) {
        throw new NotFoundException("Multiple riders found for provided phone number");
      } else if (providedPhoneNumbers.size() > 1) {
        throw new NotFoundException("More than one rider found when using multiple phone numbers search");
      }
    }
    return listOfFoundRiders.get(0);
  }

  private boolean isRiderSplitFare(FarePayment farePayment, User user) {
    return user.isRider() && farePayment.getRider().getUser().equals(user);
  }

  private void checkIsAdminOrPartOfRide(Ride ride, User currentUser, boolean failOnCompleted) throws NotFoundException, ForbiddenException {
    if (ride == null) {
      throw new NotFoundException("This ride does not exist");
    }
    if (ride.getCompletedOn() != null && failOnCompleted) {
      throw new ForbiddenException("This ride is already completed");
    }
    if (ride.getCancelledOn() != null && failOnCompleted) {
      throw new ForbiddenException("This ride is already cancelled");
    }
    User user = currentUserService.getUser();
    boolean isAdminOrPartOfRide = !currentUser.isAdmin() && !(user.isRider() && ride.getRider().getUser().equals(user));
    if (isAdminOrPartOfRide) {
      throw new ForbiddenException();
    }
  }

}
