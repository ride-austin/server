package com.rideaustin.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.City;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.CityDriverType;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.ride.RideQueueToken;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.RiderCard;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.ActiveDriverDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.repo.dsl.RideQueueTokenDslRepository;
import com.rideaustin.repo.dsl.RiderDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.PendingPaymentException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.DeeplinkDto;
import com.rideaustin.rest.model.MobileRiderRideDto;
import com.rideaustin.rest.model.RideEndLocation;
import com.rideaustin.rest.model.RideRequestParams;
import com.rideaustin.rest.model.RideStartLocation;
import com.rideaustin.service.city.CityValidationService;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.payment.PendingPaymentService;
import com.rideaustin.service.ride.DriverTypeService;
import com.rideaustin.service.user.DriverTypeUtils;
import com.rideaustin.utils.CommentUtils;
import com.rideaustin.utils.dispatch.StateMachineUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RideRequestingService {

  private final CityCache cityCache;
  private final DriverTypeService driverTypeService;
  private final CityValidationService cityValidationService;
  private final RideService rideService;
  private final PendingPaymentService pendingPaymentService;
  private final RideFlowService rideFlowService;
  private final CurrentUserService currentUserService;

  private final ActiveDriverDslRepository activeDriverDslRepository;
  private final RideDslRepository rideDslRepository;
  private final RiderDslRepository riderDslRepository;
  private final RideQueueTokenDslRepository rideQueueTokenDslRepository;
  private final StateMachinePersist<States, Events, String> contextAccess;

  private final Environment environment;

  @Transactional
  public MobileRiderRideDto requestRideAsRider(RideStartLocation startLocation, RideEndLocation endLocation,
    String carCategory, Boolean inSurgeArea, Long cityId, RideRequestParams params) throws RideAustinException {

    CommentUtils.validateComment(params.getComment());
    User user = currentUserService.getUser();
    Rider rider = getUserRider(user);

    if (!pendingPaymentService.handlePendingPayments(rider)) {
      throw new PendingPaymentException("Request was blocked due to pending payment.");
    }

    final List<String> driverTypes = Optional.ofNullable(params.getDriverType())
      .map(s -> s.split(","))
      .map(Arrays::asList)
      .orElse(Collections.emptyList());
    if (driverTypes.isEmpty()) {
      cityValidationService.validateCity(startLocation, null, cityId);
    } else {
      for (String driverType : driverTypes) {

        if (driverType != null && !driverTypeService.isDriverTypeExist(driverType)) {
          throw new BadRequestException("Driver type not found");
        }
        if (driverType != null && !driverTypeService.isCitySupportDriverType(driverType, cityId)) {
          throw new BadRequestException(String.format("Current city does not support %s", driverType));
        }

        final CityDriverType cityDriverType = Optional.ofNullable(driverType)
          .map(Collections::singleton)
          .map(DriverTypeUtils::toBitMask)
          .map(b -> driverTypeService.getCityDriverType(b, cityId))
          .map(Optional::get)
          .orElse(null);

        cityValidationService.validateCity(startLocation, cityDriverType, cityId);
      }
    }

    // Check if the rider is enabled & active
    if (!user.isEnabled() || !rider.isActive()) {
      throw new BadRequestException("Your account is not active - please contact support@example.com.");
    }
    RiderCard primaryCard = rider.getPrimaryCard();
    if (StringUtils.isBlank(params.getApplePayToken())) {
      if (primaryCard == null) {
        throw new BadRequestException("Please setup a payment method before requesting a ride");
      }
      rideService.checkIfCardLocked(primaryCard);
    }
    if (user.isDriver()) {
      ActiveDriver activeDriver = activeDriverDslRepository.findByUserAndNotInactive(user);
      if (activeDriver != null) {
        throw new BadRequestException("Active drivers cannot request rides");
      }
    }

    avoidTwoOngoingRides(rider);
    Ride ride = rideFlowService.requestRide(rider, startLocation, endLocation, carCategory, inSurgeArea, cityId, params);
    return rideDslRepository.findRiderRideInfo(ride.getId());
  }

  @Transactional
  public DeeplinkDto requestRideAsApiClient(RideStartLocation startLocation, RideEndLocation endLocation, String carCategory, Long cityId) throws RideAustinException {
    final City city = cityCache.getCity(cityId);
    final RideQueueToken rideQueueToken = rideFlowService.requestRide(startLocation, endLocation, carCategory, cityId);
    rideQueueTokenDslRepository.save(rideQueueToken);
    return new DeeplinkDto(city.getPlayStoreLink(), city.getAppStoreLink(), rideQueueToken.getToken());
  }

  private Rider getUserRider(User user) throws BadRequestException {
    List<Rider> riders = riderDslRepository.findByUserWithDependencies(user);
    if (riders == null || riders.size() != 1) {
      throw new BadRequestException("Invalid rider configuration");
    }
    return riders.get(0);
  }

  private void avoidTwoOngoingRides(Rider rider) throws BadRequestException {
    List<Ride> onGoingRides = rideDslRepository.findByRiderAndStatus(rider, RideStatus.ONGOING_RIDER_STATUSES);
    if (!rider.isDispatcherAccount() && onGoingRides != null && !onGoingRides.isEmpty()) {
      boolean shouldThrow = true;
      if (onGoingRides.size() == 1) {
        long ongoingId = onGoingRides.get(0).getId();
        shouldThrow = !Optional.ofNullable(StateMachineUtils.getPersistedContext(environment, contextAccess, ongoingId))
          .map(StateMachineContext::getState)
          .map(States.ENDED::equals)
          .orElse(true);
      }
      if (shouldThrow) {
        throw new BadRequestException("Rider has another ongoing ride");
      }
    }
  }

}
