package com.rideaustin.service;

import static com.rideaustin.utils.SafeZeroUtils.safeZero;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.core.env.Environment;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.maps.model.LatLng;
import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.Address;
import com.rideaustin.model.Campaign;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.promocodes.Promocode;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.splitfare.FarePayment;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.RiderCard;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.ActiveDriverDslRepository;
import com.rideaustin.repo.dsl.PromocodeRedemptionDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.ForbiddenException;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.DispatcherAccountRideDto;
import com.rideaustin.rest.model.MobileDriverRideDto;
import com.rideaustin.rest.model.MobileRiderRideDto;
import com.rideaustin.rest.model.MobileRiderRideDto.PrecedingRide;
import com.rideaustin.rest.model.RideEndLocation;
import com.rideaustin.rest.model.RideLocation;
import com.rideaustin.rest.model.RideStartLocation;
import com.rideaustin.service.config.StackedRidesConfig;
import com.rideaustin.service.farepayment.FarePaymentService;
import com.rideaustin.service.location.enums.LocationType;
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.service.model.ETACalculationInfo;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.service.payment.PaymentEmailService;
import com.rideaustin.service.ride.RideOwnerService;
import com.rideaustin.service.user.CarTypesCache;
import com.rideaustin.utils.dispatch.StateMachineUtils;
import com.rideaustin.utils.map.LocationCorrector;
import com.rideaustin.utils.map.LocationCorrectorConfiguration.PickupHint.DesignatedPickup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RideService {

  private static final String RIDE_DOES_NOT_EXIST_MESSAGE = "This ride does not exist";
  static final long FALLBACK_ETA = 60L;

  private final ObjectMapper mapper;

  private final RideDslRepository rideDslRepository;
  private final CurrentUserService currentUserService;
  private final MapService mapService;
  private final RiderCardService riderCardService;
  private final RideOwnerService rideOwnerService;
  private final ActiveDriverLocationService activeDriverLocationService;
  private final RiderLocationService riderLocationService;
  private final CampaignService campaignService;
  private final ActiveDriverDslRepository activeDriverDslRepository;
  private final PaymentEmailService paymentEmailService;
  private final FarePaymentService farePaymentService;
  private final PromocodeRedemptionDslRepository promocodeRedemptionDslRepository;

  private final LocationCorrector locationCorrector;

  private final ConfigurationItemCache configurationItemCache;
  private final CarTypesCache carTypesCache;

  private final StackedDriverRegistry stackedDriverRegistry;

  private final StackedRidesConfig stackedRidesConfig;

  private final StateMachinePersist<States, Events, String> contextAccess;
  private final Environment environment;

  public MobileRiderRideDto getCurrentRideAsRider() {
    User user = currentUserService.getUser();
    return rideDslRepository.findOngoingRideForRider(user);
  }

  public List<DispatcherAccountRideDto> getCurrentRidesAsDispatcher() {
    User user = currentUserService.getUser();
    List<DispatcherAccountRideDto> rides;
    if (user.getAvatar(Rider.class).isDispatcherAccount()) {
      rides = rideDslRepository.findOngoingRidesForDispatcher(user);
    } else {
      rides = Collections.emptyList();
    }
    return rides;
  }

  public MobileDriverRideDto getCurrentRideAsDriver() throws BadRequestException {
    User user = currentUserService.getUser();
    ActiveDriver activeDriver = activeDriverDslRepository.findByUserAndNotInactive(user);
    if (activeDriver == null) {
      log.info(String.format("[CURRENT] User %d: activeDriver is not found", user.getId()));
      return null;
    }
    long activeDriverId = activeDriver.getId();
    log.info(String.format("[CURRENT] User %d: activeDriver %d", user.getId(), activeDriverId));
    OnlineDriverDto onlineDriver = activeDriverLocationService.getById(activeDriverId, LocationType.ACTIVE_DRIVER);
    if (onlineDriver == null || onlineDriver.getStatus() != ActiveDriverStatus.RIDING) {
      log.info(String.format("[CURRENT] AD %d: Cached driver is not found or doesn't have RIDING status", activeDriverId));
      return null;
    }
    List<MobileDriverRideDto> ongoingRides = rideDslRepository.findCurrentForDriver(activeDriver);
    Map<RideStatus, List<MobileDriverRideDto>> ongoing = ongoingRides.stream().collect(Collectors.groupingBy(MobileDriverRideDto::getStatus));
    if (ongoing.isEmpty()) {
      MobileDriverRideDto fallback = getCurrentRideFallback(activeDriverId);
      if (fallback == null) {
        return null;
      } else {
        ongoing.put(fallback.getStatus(), Collections.singletonList(fallback));
      }
    }
    return fulfillStackedRidesInfo(activeDriverId, ongoing);
  }

  @Nonnull
  public Ride getRide(long id) throws NotFoundException {
    Ride ride = rideDslRepository.findOne(id);
    if (ride == null) {
      throw new NotFoundException(RIDE_DOES_NOT_EXIST_MESSAGE);
    }
    return ride;
  }

  public void checkIfCardLocked(RiderCard card) throws BadRequestException {
    riderCardService.checkIfCardLockedWithFail(card);
  }

  public void fillStartLocation(Ride ride, RideStartLocation startLocation) throws RideAustinException {
    Address start = getAddress(startLocation, true);
    ride.fillStartLocation(startLocation, start);
  }

  public void fillEndLocation(Ride ride, RideEndLocation endLocation, boolean useGeocoding) throws RideAustinException {
    Address end = getAddress(endLocation, useGeocoding);
    ride.fillEndLocation(endLocation, end);
  }

  public MobileRiderRideDto getRideAsRider(long id, Double latitude, Double longitude) throws ForbiddenException {
    MobileRiderRideDto ride;
    if (rideOwnerService.isRideRider(id)) {
      ride = rideDslRepository.findRiderRideInfo(id);
      // If the ride's status is DRIVER_ASSIGNED, then compute the time needed
      // to reach the ride's start location
      if (EnumSet.of(RideStatus.DRIVER_ASSIGNED, RideStatus.DRIVER_REACHED).contains(ride.getStatus()) && ride.getActiveDriver() != null) {
        riderLocationService.processLocationUpdate(ride.getRiderId(), ride.getActiveDriver().getDriver().getId(), latitude, longitude);
      }
      if (ride.getStatus() == RideStatus.COMPLETED) {
        fillCompletedRideInfo(id, ride);
      }
    } else {
      throw new ForbiddenException("You can't access this ride");
    }
    return ride;
  }

  public DispatcherAccountRideDto getRideAsDispatcher(long id) throws ForbiddenException {
    DispatcherAccountRideDto ride;
    if (rideOwnerService.isRideRider(id)) {
      ride = rideDslRepository.findDispatcherRideInfo(id);
      if (ride.getStatus() == RideStatus.COMPLETED) {
        fillCompletedRideInfo(id, ride);
      }
    } else {
      throw new ForbiddenException("You can't access this ride");
    }
    return ride;
  }

  public long getDrivingTimeToRider(long rideId) {
    ETACalculationInfo etaCalculationInfo = rideDslRepository.getETACalculationInfo(rideId);
    if (etaCalculationInfo == null) {
      return FALLBACK_ETA;
    }
    OnlineDriverDto onlineDriverDto = activeDriverLocationService.getById(etaCalculationInfo.getActiveDriverId(), LocationType.ACTIVE_DRIVER);
    if (onlineDriverDto == null) {
      return FALLBACK_ETA;
    }
    PrecedingRide precedingRide = rideDslRepository.findPrecedingRide(rideId);
    LatLng driverLocation = new LatLng(onlineDriverDto.getLatitude(), onlineDriverDto.getLongitude());
    LatLng pickupLocation = new LatLng(etaCalculationInfo.getStartLat(), etaCalculationInfo.getStartLng());
    if (precedingRide == null) {
      return mapService.getTimeToDriveCached(rideId, driverLocation, pickupLocation);
    } else {
      LatLng precedingRideEnd = new LatLng(precedingRide.getEnd().getLatitude(), precedingRide.getEnd().getLongitude());
      Long etc = Optional.ofNullable(mapService.getTimeToDriveCached(precedingRide.getId(), driverLocation, precedingRideEnd)).orElse(FALLBACK_ETA);
      Long eta = Optional.ofNullable(mapService.getTimeToDriveCached(rideId, precedingRideEnd, pickupLocation)).orElse(FALLBACK_ETA);
      int dropoffExpectation = stackedRidesConfig.getStackingDropoffExpectation();
      return etc + eta + dropoffExpectation;
    }
  }

  public MobileRiderRideDto getLastUnratedRide() {
    return rideDslRepository.findLastUnratedRide(currentUserService.getUser());
  }

  public List<DispatcherAccountRideDto> getLastUnratedRides() {
    return rideDslRepository.findLastUnratedRides(currentUserService.getUser());
  }

  public void resendReceipt(long id) throws RideAustinException {
    final Ride ride = rideDslRepository.findOne(id);
    Promocode promocode = null;
    if (ride.getPromocodeRedemptionId() != null) {
      promocode = promocodeRedemptionDslRepository.findOne(ride.getPromocodeRedemptionId()).getPromocode();
    }
    final FarePayment farePayment = farePaymentService.getFarePaymentForRide(ride);
    final List<FarePayment> secondaryPayments = farePaymentService.getAcceptedPaymentParticipants(id)
      .stream()
      .filter(fp -> !fp.isMainRider())
      .collect(Collectors.toList());
    paymentEmailService.sendEndRideEmail(ride, farePayment, secondaryPayments, promocode);
  }

  private MobileDriverRideDto fulfillStackedRidesInfo(long activeDriverId, Map<RideStatus, List<MobileDriverRideDto>> ongoing) throws BadRequestException {
    MobileDriverRideDto ride;
    if (ongoing.size() == 1) {
      ride = ongoing.values().iterator().next().get(0);
      log.info(String.format("[CURRENT] AD %d: Found single ongoing ride %d", activeDriverId, ride.getId()));
    } else if (ongoing.size() == 2 && ongoing.keySet().contains(RideStatus.ACTIVE)) {
      if (!stackedDriverRegistry.isStacked(activeDriverId)) {
        stackedDriverRegistry.addStacked(activeDriverId);
      }
      ride = ongoing.get(RideStatus.ACTIVE).get(0);
      if (ongoing.keySet().contains(RideStatus.DRIVER_ASSIGNED)) {
        List<MobileDriverRideDto> assigned = ongoing.get(RideStatus.DRIVER_ASSIGNED);
        setNextRide(ride, assigned);
        log.info(String.format("[CURRENT] AD %d: Found two ongoing rides ACTIVE %d and DA %d", activeDriverId, ride.getId(), ride.getNextRide().getId()));
      } else if (ongoing.keySet().contains(RideStatus.DRIVER_REACHED)) {
        List<MobileDriverRideDto> reached = ongoing.get(RideStatus.DRIVER_REACHED);
        setNextRide(ride, reached);
        log.info(String.format("[CURRENT] AD %d: Found two ongoing rides ACTIVE %d and DR %d", activeDriverId, ride.getId(), ride.getNextRide().getId()));
      } else {
        throw new BadRequestException("You have more than one active ride. Please contact support");
      }
    } else {
      throw new BadRequestException("You have more than two assigned rides. Please contact support");
    }
    return ride;
  }

  private void setNextRide(MobileDriverRideDto ride, List<MobileDriverRideDto> nextRides) {
    nextRides.sort(Comparator.comparing(MobileDriverRideDto::getId));
    ride.setNextRide(nextRides.get(0));
  }

  private MobileDriverRideDto getCurrentRideFallback(Long activeDriverId) {
    log.info(String.format("[CURRENT][FALLBACK]AD %d: Trying to get current ride as a fallback", activeDriverId));
    Long fallbackRideId = rideDslRepository.getRidesByStatusAndCreateDate(new Date(), EnumSet.of(RideStatus.REQUESTED, RideStatus.DRIVER_ASSIGNED))
      .stream()
      .map(Ride::getId)
      .map(id -> StateMachineUtils.getPersistedContext(environment, contextAccess, id))
      .filter(Objects::nonNull)
      .map(StateMachineContext::getExtendedState)
      .map(StateMachineUtils::getDispatchContext)
      .filter(
        ctx -> Optional.ofNullable(ctx)
          .map(DispatchContext::getCandidate)
          .map(DispatchCandidate::getId)
          .map(activeDriverId::equals)
          .orElse(false)
      )
      .map(DispatchContext::getId)
      .findAny()
      .orElse(null);
    if (fallbackRideId == null) {
      log.info(String.format("[CURRENT][FALLBACK]AD %d: Fallback ride is not found", activeDriverId));
      return null;
    }
    log.info(String.format("[CURRENT][FALLBACK]AD %d: Fallback ride %d", activeDriverId, fallbackRideId));
    return rideDslRepository.findOneForDriver(fallbackRideId);
  }

  private void fillCompletedRideInfo(long id, MobileRiderRideDto ride) {
    final Optional<Campaign> campaign = campaignService.findMatchingCampaignForRide(id);
    final CarType carType = carTypesCache.getCarType(ride.getCarType());
    final CarType.Configuration carTypeConfig = carType.getConfigurationObject(mapper);
    final boolean carTypeTippingEnabled = carTypeConfig.isTippingEnabled();
    final Integer ridePaymentDelay = safeZero(configurationItemCache.getConfigAsInt(ClientType.RIDER, "tipping", "ridePaymentDelay"));
    final boolean tippingPeriodActive = ride.getCompletedOn().toInstant().plus(ridePaymentDelay, ChronoUnit.SECONDS).isAfter(Instant.now());
    boolean campaignTippingEnabled = true;
    if (campaign.isPresent()) {
      ride.setTotalFare(campaign.get().adjustTotalCharge(ride.getTotalCharge()));
      campaignTippingEnabled = campaign.get().isTippingAllowed();
    }
    ride.setTippingAllowed(carTypeTippingEnabled && tippingPeriodActive && campaignTippingEnabled);
    ride.setTipUntil(DateUtils.addSeconds(ride.getCompletedOn(), ridePaymentDelay));
  }

  @Nullable
  private Address getAddress(@Nonnull RideLocation location, boolean useGeocoding) throws RideAustinException {
    Address result = null;
    if (StringUtils.isNotEmpty(location.getGooglePlaceId())) {
      result = mapService.retrieveAddress(location.getGooglePlaceId());
    } else if (StringUtils.isNotBlank(location.getAddress())) {
      result = new Address(location.getAddress(), location.getZipCode());
    } else if (useGeocoding) {
      result = mapService.reverseGeocodeAddress(location.getLat(), location.getLng());
    }
    final Optional<DesignatedPickup> designatedPickup = location.asLatLng().isPresent()
      ? locationCorrector.correctLocation(location.asLatLng().get())
      : Optional.empty();
    if (designatedPickup.isPresent() && result != null) {
      result.concat(String.format(" (%s)", designatedPickup.map(DesignatedPickup::getName).orElse("")));
    }
    return result;
  }

}
