package com.rideaustin.service.farepayment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import org.joda.money.Money;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.Constants;
import com.rideaustin.model.Campaign;
import com.rideaustin.model.enums.PaymentStatus;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.enums.SplitFareStatus;
import com.rideaustin.model.ride.FareDetails;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.splitfare.FarePayment;
import com.rideaustin.repo.dsl.FarePaymentDslRepository;
import com.rideaustin.rest.exception.ConflictException;
import com.rideaustin.rest.model.FarePaymentDto;
import com.rideaustin.rest.model.PagingParams;
import com.rideaustin.service.FareService;
import com.rideaustin.service.FareService.CampaignCoverageResult;
import com.rideaustin.service.model.PendingPaymentDto;
import com.rideaustin.service.promocodes.PromocodeUseResult;
import com.rideaustin.utils.FareUtils;
import com.rideaustin.utils.SafeZeroUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FarePaymentService {

  private final FarePaymentDslRepository farePaymentDslRepository;
  private final FareService fareService;

  public FarePayment createFarePaymentForMainRider(Ride ride) throws ConflictException {
    FarePayment alreadyCreatedFarePaymentForRider = farePaymentDslRepository.findFarePayment(ride.getId(), ride.getRider().getId());
    if (null != alreadyCreatedFarePaymentForRider && alreadyCreatedFarePaymentForRider.isMainRider()) {
      log.error("There was already main rider created for a ride (for proper rider). ");
      return alreadyCreatedFarePaymentForRider;
    } else if (null != alreadyCreatedFarePaymentForRider) {
      throw new ConflictException("This ride has already created fare payment for ride main ride, but not as main rider");
    }
    Optional<FarePayment> existing = farePaymentDslRepository.findMainRiderFarePayment(ride.getId());
    if (existing.isPresent()) {
      throw new ConflictException("This ride has already created fare payment for other rider as main rider ");
    }

    FarePayment mainRiderFarePayment = FarePayment.builder()
      .ride(ride)
      .rider(ride.getRider())
      .mainRider(true)
      .splitStatus(SplitFareStatus.ACCEPTED)
      .build();
    mainRiderFarePayment = farePaymentDslRepository.save(mainRiderFarePayment);
    return mainRiderFarePayment;
  }

  public List<PendingPaymentDto> listPendingPayments(long riderId) {
    return farePaymentDslRepository.listPendingPaymentsForRider(riderId);
  }

  public void updateFarePayment(FarePayment farePayment) {
    farePaymentDslRepository.save(farePayment);
  }

  public List<FarePayment> getAcceptedPaymentParticipants(Long rideId) {
    return farePaymentDslRepository.findAcceptedFarePayments(rideId);
  }

  public List<FarePaymentDto> getAcceptedPaymentParticipantsInfo(Long rideId) {
    return farePaymentDslRepository.findAcceptedFarePaymentInfo(rideId);
  }

  public Page<FarePayment> getRiderPaymentHistory(@Nonnull Long riderId, @Nullable RideStatus status, @Nonnull PagingParams paging) {
    return farePaymentDslRepository.getRidePaymentHistoryFarePayment(riderId, status, paging);
  }

  public FarePaymentInfo createFarePaymentInfo(Ride ride, PromocodeUseResult promocodeUseResult, boolean useEstimate) throws ConflictException {

    FarePaymentInfo info;
    if (ride.isUserCancelled()) {
      Optional<FareDetails> fareDetails = fareService.calculateFinalFare(ride, promocodeUseResult, ride.getPaymentStatus() == PaymentStatus.PREPAID_UPFRONT);
      Money cancellationFee = fareDetails.map(FareDetails::getCancellationFee).orElse(Constants.ZERO_USD);
      info = FarePaymentInfo.builder()
        .primaryRiderFarePayment(getMainFarePayment(ride))
        .secondaryRiderPayments(new ArrayList<>())
        .farePerParticipants(cancellationFee)
        .primaryRiderFare(cancellationFee)
        .build();
    } else {
      List<FarePayment> paymentParticipants = getAcceptedPaymentParticipants(ride.getId());
      ParticipantsFare participantsFare = calculateAmountToPayPerParticipant(ride, paymentParticipants, promocodeUseResult, useEstimate);
      info = FarePaymentInfo.builder()
        .primaryRiderFarePayment(getMainRiderFarePayment(paymentParticipants, ride))
        .secondaryRiderPayments(getNoMainRiderFarePayments(paymentParticipants))
        .farePerParticipants(participantsFare.getSecondaryRidersFare())
        .primaryRiderFare(participantsFare.getPrimaryRiderFare())
        .build();
    }
    return info;
  }

  private FarePayment getMainFarePayment(Ride ride) {
    return farePaymentDslRepository.findFarePayment(ride.getId(), ride.getRider().getId());
  }

  private List<FarePayment> getNoMainRiderFarePayments(List<FarePayment> farePayments) {
    return farePayments.stream().filter(fp -> !fp.isMainRider()).collect(Collectors.toList());
  }

  private FarePayment getMainRiderFarePayment(List<FarePayment> farePayments, Ride ride) throws ConflictException {
    for (FarePayment fp : farePayments) {
      if (fp.isMainRider()) {
        if (!fp.getRider().equals(ride.getRider())) {
          throw new ConflictException("Main rider on fare payment is different that main rider of ride");
        }
        return fp;
      }
    }
    return null;
  }

  private ParticipantsFare calculateAmountToPayPerParticipant(Ride ride, List<FarePayment> allAcceptedFarePayments, PromocodeUseResult promocodeUseResult, boolean useEstimate) throws ConflictException {
    if (allAcceptedFarePayments == null || allAcceptedFarePayments.isEmpty()) {
      throw new ConflictException("No fare payment recorded");
    }
    final Optional<FareDetails> totalFareDetails = fareService.calculateTotalFare(ride, promocodeUseResult, false, useEstimate || ride.getPaymentStatus() == PaymentStatus.PREPAID_UPFRONT);
    final CampaignCoverageResult campaignCoverageResult = fareService.checkCampaignCoverage(totalFareDetails.get(), ride);
    Optional<FareDetails> fareDetails = fareService.calculateFinalFare(ride, promocodeUseResult, useEstimate || ride.getPaymentStatus() == PaymentStatus.PREPAID_UPFRONT);
    Money noTipsCharge = fareDetails
      .map(details -> details.getTotalCharge().minus(SafeZeroUtils.safeZero(details.getTip())))
      .orElse(Constants.ZERO_USD);
    Money secondary;
    Money primary;
    if (campaignCoverageResult.isCoveredByCampaign()) {
      final Campaign campaign = campaignCoverageResult.getCampaignOptional().get();
      secondary = FareUtils.adjustStripeChargeAmount(campaign.adjustTotalCharge(noTipsCharge));
      primary = secondary;
    } else {
      secondary = FareUtils.adjustStripeChargeAmount(noTipsCharge.dividedBy(allAcceptedFarePayments.size(), Constants.ROUNDING_MODE));
      primary = FareUtils.adjustStripeChargeAmount(secondary.plus(fareDetails.map(FareDetails::getTip).orElse(Constants.ZERO_USD)));
    }
    return new ParticipantsFare(primary, secondary);
  }

  public FarePayment getFarePaymentForRide(Ride ride) {
    return farePaymentDslRepository.findFarePayment(ride.getId(), ride.getRider().getId());
  }

  @Getter
  @Builder
  public static class FarePaymentInfo {
    private final FarePayment primaryRiderFarePayment;
    private final List<FarePayment> secondaryRiderPayments;
    private final Money farePerParticipants;
    private final Money primaryRiderFare;
  }

  @Getter
  @AllArgsConstructor
  static class ParticipantsFare {
    final Money primaryRiderFare;
    final Money secondaryRidersFare;
  }
}
