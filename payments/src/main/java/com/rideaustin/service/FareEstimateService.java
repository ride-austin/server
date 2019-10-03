package com.rideaustin.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.stereotype.Service;

import com.google.maps.model.LatLng;
import com.rideaustin.Constants;
import com.rideaustin.assemblers.CampaignBannerDtoAssembler;
import com.rideaustin.model.Campaign;
import com.rideaustin.model.airports.Airport;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.ride.CityCarType;
import com.rideaustin.model.ride.FareDetails;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Rider;
import com.rideaustin.rest.model.CampaignBannerDto;
import com.rideaustin.rest.model.EstimatedFareDTO;
import com.rideaustin.service.airport.AirportService;
import com.rideaustin.service.model.DistanceTime;
import com.rideaustin.service.model.DistanceTimeEstimation;
import com.rideaustin.service.promocodes.PromocodeService;
import com.rideaustin.service.promocodes.PromocodeUseRequest;
import com.rideaustin.service.promocodes.PromocodeUseResult;
import com.rideaustin.service.surgepricing.SurgePricingService;
import com.rideaustin.utils.FareUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FareEstimateService {

  private final MapService mapService;
  private final AirportService airportService;
  private final CarTypeService carTypeService;
  private final CampaignService campaignService;
  private final PromocodeService promocodeService;
  private final CurrentUserService currentUserService;
  private final SurgePricingService surgePricingService;
  private final CampaignBannerDtoAssembler campaignBannerDtoAssembler;

  public Optional<EstimatedFareDTO> estimateFare(LatLng startLocation, LatLng endLocation, CarType carType, Long cityId) {

    final DistanceTimeEstimation estimation = getDistanceTimeEstimation(startLocation, endLocation);
    if (estimation == null) {
      return Optional.empty();
    }

    final Optional<CityCarType> cityCarType = carTypeService.getCityCarType(carType.getCarCategory(), cityId);
    if (!cityCarType.isPresent()) {
      return Optional.empty();
    }
    Money defaultTotalFare = getEstimatedFare(startLocation, endLocation, cityId, estimation, cityCarType.get());
    final BigDecimal estimatedDistanceMeters = estimation.getDistanceInMiles().divide(Constants.MILES_PER_METER, Constants.ROUNDING_MODE);
    final CampaignBannerDto defaultCampaignInfo = getCampaignInfo(startLocation, endLocation, carType, defaultTotalFare,
      estimatedDistanceMeters);
    Money minimumFare = FareUtils.estimateNormalFare(cityCarType.get(), estimation.getDistanceInMiles(), estimation.getEstimatedTime());
    defaultTotalFare = applyPromocode(startLocation, carType, cityId, minimumFare, cityCarType.get(), defaultTotalFare, defaultCampaignInfo);

    final List<CityCarType> cityCarTypes = carTypeService.getCityCarTypes(cityId);
    final Map<String, Pair<Money, CampaignBannerDto>> estimates = new HashMap<>();
    for (CityCarType cct : cityCarTypes) {
      final CarType category = cct.getCarType();
      Money estimatedFare = getEstimatedFare(startLocation, endLocation, cityId, estimation, cct);
      final CampaignBannerDto campaignInfo = getCampaignInfo(startLocation, endLocation, category, estimatedFare, estimatedDistanceMeters);
      minimumFare = FareUtils.estimateNormalFare(cct, estimation.getDistanceInMiles(), estimation.getEstimatedTime());
      estimatedFare = applyPromocode(startLocation, category, cityId, minimumFare, cct, estimatedFare, campaignInfo);
      estimates.put(category.getCarCategory(), ImmutablePair.of(estimatedFare, campaignInfo));
    }

    final EstimatedFareDTO value = new EstimatedFareDTO(estimation, defaultTotalFare, defaultCampaignInfo, estimates);
    return Optional.of(value);
  }

  public Optional<FareDetails> estimateFare(Ride ride) {
    if (ride.getEndLocationLat() == null && ride.getEndLocationLong() == null) {
      return Optional.empty();
    }
    Optional<CityCarType> cityCarType = getCityCarType(ride);
    if (!cityCarType.isPresent()) {
      return Optional.empty();
    }
    Optional<FareDetails> fareDetails = cityCarType.flatMap(cct -> FareUtils.setBaseRates(ride, cct));
    if (!fareDetails.isPresent()) {
      return Optional.empty();
    }

    Optional<EstimatedFareDTO> estimatedFare = estimateFare(
      new LatLng(ride.getStartLocationLat(), ride.getStartLocationLong()),
      new LatLng(ride.getEndLocationLat(), ride.getEndLocationLong()),
      ride.getRequestedCarType(), ride.getCityId());
    if (!estimatedFare.isPresent()) {
      return Optional.empty();
    }
    fareDetails.get().setEstimatedFare(estimatedFare.get().getDefaultEstimate().getTotalFare());
    fareDetails.get().setDistanceFare(FareUtils.calculateDistanceFare(cityCarType.get().getRatePerMile(), estimatedFare.get().getDistanceTimeEstimation().getDistanceInMiles()));
    fareDetails.get().setTimeFare(FareUtils.calculateTimeFare(cityCarType.get().getRatePerMinute(), estimatedFare.get().getDistanceTimeEstimation().getEstimatedTime()));
    return fareDetails;
  }

  private DistanceTimeEstimation getDistanceTimeEstimation(LatLng startLocation, LatLng endLocation) {
    // Get the distance/traffic time
    DistanceTime distanceTime;
    try {
      distanceTime = mapService.computeDistanceTime(startLocation, endLocation);
    } catch (MapException exception) {
      log.error("Error while estimating distance and time", exception);
      return null;
    }
    BigDecimal distanceInMiles = Constants.MILES_PER_METER.multiply(BigDecimal.valueOf(distanceTime.getDistance()));
    BigDecimal estimatedTime = Constants.MINUTES_PER_SECOND.multiply(BigDecimal.valueOf(distanceTime.getTime()));
    return new DistanceTimeEstimation(distanceTime, distanceInMiles, estimatedTime);
  }

  private Money applyPromocode(LatLng startLocation, CarType carType, Long cityId, Money normalFare, CityCarType cityCarType, Money defaultTotalFare, CampaignBannerDto campaignInfo) {
    final Rider rider = currentUserService.getUser().getAvatar(Rider.class);
    final Money discount;
    if (rider != null) {
      BigDecimal surgeFactor = surgePricingService.getSurgeFactor(startLocation, cityCarType.getCarType(), cityId);
      final BigDecimal fareCreditAmount = normalFare
        .multipliedBy(surgeFactor, Constants.ROUNDING_MODE)
        .getAmount();
      final BigDecimal rideCreditAmount = fareCreditAmount
        .add(cityCarType.getBookingFee().getAmount())
        .add(FareUtils.calculateProcessingFee(cityCarType).getAmount());
      PromocodeUseRequest request = new PromocodeUseRequest(rider.getId(), cityId, carType.getCarCategory(), fareCreditAmount, rideCreditAmount, campaignInfo != null);
      final PromocodeUseResult promocodeUseResult = promocodeService.usePromocode(request, true);
      discount = Money.of(CurrencyUnit.USD, promocodeUseResult.getPromocodeCreditUsed());
    } else {
      discount = Constants.ZERO_USD;
    }
    return defaultTotalFare.minus(discount);
  }

  private CampaignBannerDto getCampaignInfo(LatLng startLocation, LatLng endLocation, CarType carType, Money totalFare, BigDecimal distance) {
    final Optional<Campaign> eligibleCampaign = campaignService.findEligibleCampaign(new Date(), startLocation,
      endLocation, carType.getCarCategory(), currentUserService.getUser().getAvatar(Rider.class), distance);
    final Optional<Money> fareLimit = eligibleCampaign.map(Campaign::getMaximumCappedAmount);
    if (fareLimit.map(totalFare::isLessThan).orElse(false) || fareLimit.map(totalFare::isEqual).orElse(false)) {
      return eligibleCampaign
        .map(campaignBannerDtoAssembler::toDto)
        .map(c -> {
          final Money campaignFare = eligibleCampaign.get().adjustTotalCharge(totalFare);
          c.setEstimatedFare(campaignFare);
          return c;
        })
        .orElse(null);
    }
    return null;
  }

  private Money getEstimatedFare(LatLng startLocation, LatLng endLocation, Long cityId, DistanceTimeEstimation estimation, CityCarType cityCarType) {
    BigDecimal surgeFactor = surgePricingService.getSurgeFactor(startLocation, cityCarType.getCarType(), cityId);
    log.info("Estimated total distance is in meters: {}", estimation.getDistanceTime().getDistance());
    log.info("Estimated total time in seconds: {}", estimation.getDistanceTime().getTime());

    Money airportFee = calculateAirportFee(startLocation, endLocation);
    return FareUtils.estimateFare(cityCarType, estimation.getDistanceInMiles(), estimation.getEstimatedTime(), surgeFactor, airportFee);
  }

  private Money calculateAirportFee(LatLng startLocation, LatLng endLocation) {
    Money airportPickupFee = airportService.getAirportForLocation(startLocation).map(Airport::getPickupFee).orElse(Constants.ZERO_USD);
    Money airportDropoffFee = airportService.getAirportForLocation(endLocation).map(Airport::getDropoffFee).orElse(Constants.ZERO_USD);
    Money airportFee;
    if (airportPickupFee.isPositive() && airportDropoffFee.isPositive()) {
      airportFee = airportPickupFee;
    } else {
      airportFee = airportPickupFee.plus(airportDropoffFee);
    }
    return airportFee;
  }

  private Optional<CityCarType> getCityCarType(@Nonnull Ride ride) {
    return carTypeService.getCityCarTypeWithFallback(ride.getRequestedCarType(), ride.getCityId());
  }
}
