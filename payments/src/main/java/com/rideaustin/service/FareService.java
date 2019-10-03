package com.rideaustin.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.core.env.Environment;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.stereotype.Service;

import com.google.maps.model.LatLng;
import com.rideaustin.Constants;
import com.rideaustin.model.Campaign;
import com.rideaustin.model.Charity;
import com.rideaustin.model.airports.Airport;
import com.rideaustin.model.enums.CampaignCoverageType;
import com.rideaustin.model.enums.PaymentStatus;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.fee.SpecialFee;
import com.rideaustin.model.ride.CityCarType;
import com.rideaustin.model.ride.FareDetails;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.service.airport.AirportService;
import com.rideaustin.service.config.RidePaymentConfig;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.RideFlowContext;
import com.rideaustin.service.promocodes.PromocodeService;
import com.rideaustin.service.promocodes.PromocodeUseRequest;
import com.rideaustin.service.promocodes.PromocodeUseResult;
import com.rideaustin.utils.FareUtils;
import com.rideaustin.utils.SafeZeroUtils;
import com.rideaustin.utils.dispatch.StateMachineUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FareService {

  private final CarTypeService carTypeService;
  private final AirportService airportService;
  private final PromocodeService promocodeService;
  private final CampaignService campaignService;
  private final RidePaymentConfig config;
  private final RideDslRepository rideDslRepository;

  private final Environment environment;
  private final StateMachinePersist<States, Events, String> contextAccess;

  /**
   * Calculate total fare. Include everything, except tips
   *
   * @param ride
   * @param freeCreditOverride
   * @return
   */
  public Optional<FareDetails> calculateTotalFare(Ride ride, PromocodeUseResult freeCreditOverride) {
    return calculateTotalFare(ride, freeCreditOverride, false);
  }

  public Optional<FareDetails> calculateTotalFare(Ride ride, PromocodeUseResult freeCreditOverride, boolean skipRoundup) {
    return calculateTotalFare(ride, freeCreditOverride, skipRoundup, false);
  }

  public Optional<FareDetails> calculateTotalFare(Ride ride, PromocodeUseResult freeCreditOverride, boolean skipRoundup, boolean useEstimate) {
    Optional<CityCarType> cityCarType = getCityCarType(ride);
    if (!cityCarType.isPresent()) {
      return Optional.empty();
    }
    Optional<FareDetails> detailsOptional = cityCarType.flatMap(cct -> FareUtils.setBaseRates(ride, cct));
    if (!detailsOptional.isPresent()) {
      return Optional.empty();
    }
    Money distanceFare;
    Money timeFare;
    if (useEstimate) {
      distanceFare = ride.getDistanceFare();
      timeFare = ride.getTimeFare();
    } else {
      BigDecimal milesTravelled = Constants.MILES_PER_METER.multiply(Optional.ofNullable(ride.getDistanceTravelled()).orElse(BigDecimal.ZERO));
      BigDecimal rideDuration = ride.getDuration();
      distanceFare = FareUtils.calculateDistanceFare(cityCarType.get().getRatePerMile(), milesTravelled);
      timeFare = FareUtils.calculateTimeFareMillis(cityCarType.get().getRatePerMinute(), rideDuration);
    }

    FareDetails fareDetails = detailsOptional.get();
    fareDetails.setDistanceFare(distanceFare);
    fareDetails.setTimeFare(timeFare);
    fareDetails.setNormalFare(FareUtils.calculateNormalFare(fareDetails));
    if (notSet(fareDetails.getSurgeFare())) {
      fareDetails.setSurgeFare(FareUtils.calculateSurgeFare(fareDetails, ride));
    }

    Money subTotal = FareUtils.calculateSubTotal(fareDetails);
    fareDetails.setSubTotal(subTotal);
    Money fixedRAFee = FareUtils.calculateRAFee(cityCarType.get(), subTotal);
    fareDetails.setDriverPayment(subTotal.minus(fixedRAFee));
    fareDetails.setRaPayment(fixedRAFee);

    Money airportPickupFee = airportService.getAirportForLocation(ride.getStartLocationLat(), ride.getStartLocationLong())
      .map(Airport::getPickupFee).orElse(Constants.ZERO_USD);
    Money airportDropoffFee = airportService.getAirportForLocation(ride.getEndLocationLat(), ride.getEndLocationLong())
      .map(Airport::getDropoffFee).orElse(Constants.ZERO_USD);
    Money airportFee = airportPickupFee.plus(airportDropoffFee);
    fareDetails.setAirportFee(airportFee);
    Money cityFeeBase = subTotal.plus(fareDetails.getBookingFee())
      .plus(airportFee);
    fareDetails.setCityFee(FareUtils.calculateCityFee(cityFeeBase, cityCarType.get()));

    Money processingFee = FareUtils.calculateProcessingFee(cityCarType.get());
    Money freeCreditUsed = Constants.ZERO_USD;

    if (freeCreditOverride != null) {
      freeCreditUsed = Money.of(CurrencyUnit.USD, freeCreditOverride.getPromocodeCreditUsed());
    } else if (ride.getFreeCreditCharged() != null) {
      freeCreditUsed = ride.getFreeCreditCharged();
    }
    fareDetails.setProcessingFee(processingFee);
    fareDetails.setFreeCreditCharged(freeCreditUsed);

    Money totalFare = FareUtils.calculateTotalFare(fareDetails);
    fareDetails.setTotalFare(totalFare);

    if (!skipRoundup) {
      Money roundUpAmount = Constants.ZERO_USD;
      //add round up amount if charity is chosen
      Charity charity = rideDslRepository.findCharity(ride);
      if (charity != null) {
        roundUpAmount = FareUtils.calculateRoundUp(totalFare);
      }
      fareDetails.setRoundUpAmount(roundUpAmount);
    }

    return Optional.of(fareDetails);
  }

  private boolean notSet(Money amount) {
    return SafeZeroUtils.safeZero(amount).isZero();
  }

  public Optional<FareDetails> calculateFinalFare(Ride ride, PromocodeUseResult freeCreditOverride) {
    return calculateFinalFare(ride, freeCreditOverride, false);
  }

  /**
   * Calculate final fare. Include everything and set {@link FareDetails#setStripeCreditCharge(Money)}
   *
   * @param ride
   * @param freeCreditOverride
   * @return
   */
  public Optional<FareDetails> calculateFinalFare(Ride ride, PromocodeUseResult freeCreditOverride, boolean useEstimate) {
    boolean isCancelledRide = ride.isUserCancelled();
    Optional<FareDetails> detailsOptional;
    if (isCancelledRide) {
      detailsOptional = processCancellation(ride, shouldChargeCancellationFee(ride));
    } else {
      detailsOptional = calculateTotalFare(ride, freeCreditOverride, false, useEstimate);
    }
    if (!detailsOptional.isPresent()) {
      return Optional.empty();
    }

    FareDetails fareDetails = detailsOptional.get();
    Money totalCharge;
    if (isCancelledRide) {
      totalCharge = fareDetails.getTotalFare();
    } else {
      CampaignCoverageResult campaignCoverageResult = checkCampaignCoverage(fareDetails, ride);
      Optional<Campaign> campaignOptional = campaignCoverageResult.getCampaignOptional();
      boolean isCoveredByCampaign = campaignCoverageResult.isCoveredByCampaign();
      if (isCoveredByCampaign) {
        final Campaign campaign = campaignOptional.get();
        campaignService.addRide(campaign, ride);
        fareDetails.setProcessingFee(Constants.ZERO_USD);
        fareDetails.setTotalFare(FareUtils.calculateTotalFare(fareDetails));
        totalCharge = campaign.adjustTotalCharge(fareDetails.getTotalCharge());
        if (!campaign.isTippingAllowed()) {
          fareDetails.setTip(null);
        }
      } else {
        fareDetails.setTip(ride.getTip());
        fareDetails.setDriverPayment(fareDetails.getDriverPayment().plus(SafeZeroUtils.safeZero(fareDetails.getTip())));
        totalCharge = fareDetails.getTotalCharge();
      }
    }
    totalCharge = FareUtils.adjustStripeChargeAmount(totalCharge);

    fareDetails.setStripeCreditCharge(totalCharge);
    return Optional.of(fareDetails);
  }

  public Optional<FareDetails> processCancellation(Ride ride, boolean shouldChargeCancellationFee) {
    Optional<CityCarType> carTypeOptional = getCityCarType(ride);
    if (!carTypeOptional.isPresent()) {
      return Optional.empty();
    }
    CityCarType cityCarType = carTypeOptional.get();
    Money cancellationFee = cityCarType.getCancellationFee();

    FareDetails fareDetails = ride.getFareDetails();
    fareDetails.reset();
    if (shouldChargeCancellationFee) {
      fareDetails.setCancellationFee(cancellationFee);
      fareDetails.setSubTotal(cancellationFee);
      fareDetails.setTotalFare(cancellationFee);
      fareDetails.setDriverPayment(cancellationFee);
    }

    return Optional.of(fareDetails);
  }

  public boolean shouldChargeCancellationFee(Ride ride, RideStatus status) {
    CityCarType cityCarType = carTypeService.getCityCarTypeWithFallback(ride.getRequestedCarType(), ride.getCityId())
      .orElse(null);
    if (cityCarType == null) {
      return false;
    }
    Instant cancellationFeeTimeThreshold =
      Instant.now().minus(config.getCancellationChargeFreePeriod(), ChronoUnit.SECONDS);

    boolean nonZeroCancellationFee = cityCarType
      .getCancellationFee().isPositive();

    RideFlowContext flowContext = null;
    StateMachineContext<States, Events> persistedContext = StateMachineUtils.getPersistedContext(environment, contextAccess, ride.getId());
    if (persistedContext != null) {
      flowContext = StateMachineUtils.getFlowContext(persistedContext.getExtendedState());
    }

    Date driverAcceptedOn;
    Date driverReachedOn;
    if (flowContext != null) {
      driverAcceptedOn = flowContext.getAcceptedOn();
      driverReachedOn = flowContext.getReachedOn();
    } else {
      driverAcceptedOn = ride.getDriverAcceptedOn();
      driverReachedOn = ride.getDriverReachedOn();
    }

    boolean riderCancellationCriteria = driverAcceptedOn != null && cancellationFeeTimeThreshold
      .isAfter(driverAcceptedOn.toInstant()) && RideStatus.RIDER_CANCELLED.equals(status);

    boolean driverCancellationCriteria = driverReachedOn != null && cancellationFeeTimeThreshold
      .isAfter(driverReachedOn.toInstant()) && RideStatus.DRIVER_CANCELLED.equals(status);

    return nonZeroCancellationFee
      && (riderCancellationCriteria || driverCancellationCriteria);
  }

  public CampaignCoverageResult checkCampaignCoverage(FareDetails fareDetails, Ride ride) {
    Optional<Campaign> campaignOptional = campaignService.findMatchingCampaignForRide(ride);
    boolean isCoveredByCampaign = false;
    if (campaignOptional.isPresent()) {
      final Campaign campaign = campaignOptional.get();
      if (campaign.getCoverageType() == CampaignCoverageType.FULL) {
        if (fareDetails.getTotalCharge().isLessThan(campaign.getMaximumCappedAmount())) {
          isCoveredByCampaign = true;
        } else {
          log.info(String.format("[CAMPAIGN][Ride %d] Ride cost is %s which is greater than campaign limit %s",
            ride.getId(), fareDetails.getTotalCharge(), campaign.getMaximumCappedAmount()));
          isCoveredByCampaign = false;
        }
      } else if (campaign.getCoverageType() == CampaignCoverageType.PARTIAL) {
        isCoveredByCampaign = true;
      }
    }
    return new CampaignCoverageResult(campaignOptional.orElse(null), isCoveredByCampaign);
  }

  /**
   * Determines special fees which may be applied to ride cost depending on pickup location
   * (for example, pickup near the airport terminal).
   *
   * @param pickupLocation
   * @return
   */
  public List<SpecialFee> getSpecialFees(LatLng pickupLocation) {
    // Airport Fee
    Optional<Airport> airportOptional = airportService.getAirportForLocation(pickupLocation);
    if (airportOptional.isPresent()) {
      Airport airport = airportOptional.get();
      SpecialFee fee = SpecialFee.builder()
        .title(SpecialFee.TITLE_AIRPORT_PICKUP_SURCHARGE)
        .description("")
        .valueType(SpecialFee.ValueType.AMOUNT)
        .value(SafeZeroUtils.safeZero(airport.getPickupFee()).getAmount())
        .build();
      return Collections.singletonList(fee);
    } else {
      return Collections.emptyList();
    }
  }

  public void calculateTotals(Ride ride) {
    Optional<FareDetails> detailsOptional;
    if (ride.getSubTotal() == null || ride.getSubTotal().isZero()) {
      detailsOptional = calculateTotalFare(ride, null);
      detailsOptional.ifPresent(ride::setFareDetails);
    }
    Long riderId = rideDslRepository.getRiderId(ride);
    final Optional<Campaign> campaign = campaignService.findMatchingCampaignForRide(ride);
    PromocodeUseResult promocodeUseResult = null;
    if (ride.getPaymentStatus() != PaymentStatus.PREPAID_UPFRONT) {
      promocodeUseResult = promocodeService.usePromocode(new PromocodeUseRequest(ride, riderId, campaign.isPresent()));
      if (promocodeUseResult.isSuccess()) {
        ride.setPromocodeRedemptionId(promocodeUseResult.getAffectedPromocodeRedemption().getId());
      }
    }
    detailsOptional = calculateFinalFare(ride, promocodeUseResult, ride.getPaymentStatus() == PaymentStatus.PREPAID_UPFRONT);
    detailsOptional.ifPresent(ride::setFareDetails);
  }

  private boolean shouldChargeCancellationFee(Ride ride) {
    return shouldChargeCancellationFee(ride, ride.getStatus());
  }

  private Optional<CityCarType> getCityCarType(@Nonnull Ride ride) {
    return carTypeService.getCityCarTypeWithFallback(ride.getRequestedCarType(), ride.getCityId());
  }

  @AllArgsConstructor
  public static class CampaignCoverageResult {
    private final Campaign campaignOptional;
    @Getter
    private final boolean coveredByCampaign;

    public Optional<Campaign> getCampaignOptional() {
      return Optional.ofNullable(campaignOptional);
    }
  }

}
