package com.rideaustin.service;

import static com.rideaustin.dispatch.LogUtil.dispatchInfo;
import static com.rideaustin.dispatch.LogUtil.flowInfo;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.async.DeferredResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.maps.model.LatLng;
import com.rideaustin.config.CacheConfiguration;
import com.rideaustin.dispatch.messages.DeclineDispatchMessage;
import com.rideaustin.dispatch.messages.DeferredResultMessage;
import com.rideaustin.dispatch.messages.DispatchReachMessage;
import com.rideaustin.dispatch.messages.DriverReachMessage;
import com.rideaustin.dispatch.messages.EndRideMessage;
import com.rideaustin.dispatch.messages.RideAcceptMessage;
import com.rideaustin.dispatch.messages.RideStartMessage;
import com.rideaustin.dispatch.messages.UpdateCommentMessage;
import com.rideaustin.dispatch.messages.UpdateDestinationMessage;
import com.rideaustin.dispatch.service.RideFlowStateMachineProvider;
import com.rideaustin.model.Session;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.CancellationReason;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.redis.RedisSurgeArea;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.ride.DriverType;
import com.rideaustin.model.ride.FareDetails;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.ride.RideQueueToken;
import com.rideaustin.model.ride.RiderOverride;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.repo.dsl.RideQueueTokenDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.rest.model.MobileDriverRideDto;
import com.rideaustin.rest.model.MobileRiderRideDto;
import com.rideaustin.rest.model.RideEndLocation;
import com.rideaustin.rest.model.RideRequestParams;
import com.rideaustin.rest.model.RideStartLocation;
import com.rideaustin.service.config.RideDestinationUpdateConfig;
import com.rideaustin.service.config.RideDispatchServiceConfig;
import com.rideaustin.service.config.RidePaymentConfig;
import com.rideaustin.service.eligibility.RiderEligibilityCheckContext;
import com.rideaustin.service.eligibility.RiderEligibilityCheckService;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.RideFlowContext;
import com.rideaustin.service.model.context.RideRequestContext;
import com.rideaustin.service.ride.CarTypeRequestHandler;
import com.rideaustin.service.ride.DriverTypeService;
import com.rideaustin.service.surgepricing.SurgePricingService;
import com.rideaustin.service.user.CarTypesCache;
import com.rideaustin.utils.dispatch.StateMachineUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RideFlowService {

  private final RideDslRepository rideDslRepository;
  private final RideQueueTokenDslRepository rideQueueTokenDslRepository;

  private final CurrentUserService currentUserService;
  private final RideService rideService;
  private final FareEstimateService fareEstimateService;
  private final CurrentSessionService currentSessionService;
  private final SurgePricingService surgePricingService;
  private final PaymentJobService paymentJobService;
  private final RiderEligibilityCheckService eligibilityCheckService;
  private final CarTypesCache carTypesCache;
  private final DriverTypeService driverTypeService;
  private final ActiveDriversService activeDriversService;
  private final CancellationFeedbackService cancellationFeedbackService;
  private final RequestedDriversRegistry requestedDriversRegistry;
  private final RidePreauthorizationService preauthorizationService;

  private final RideFlowStateMachineProvider machineProvider;
  private final RideFlowServiceErrorProvider errorProvider;

  private final RideDispatchServiceConfig dispatchConfig;
  private final RidePaymentConfig paymentConfig;
  private final RideDestinationUpdateConfig destinationUpdateConfig;

  private final ObjectMapper mapper;
  private final BeanFactory beanFactory;
  private final Environment environment;

  @Transactional
  public Ride requestRide(Rider rider, RideStartLocation startLocation, RideEndLocation endLocation, String carCategory,
    Boolean inSurgeArea, Long cityId, RideRequestParams params) throws RideAustinException {
    User user = currentUserService.getUser();
    Session riderSession = currentSessionService.getCurrentSession(user);

    CarType requestedCarType = carTypesCache.getCarType(carCategory);
    CarType.Configuration carTypeConfig = requestedCarType.getConfigurationObject(mapper, CarType.Configuration.class);

    if (carTypeConfig.getRequestHandlerClass() != null) {
      CarTypeRequestHandler handler = beanFactory.getBean(carTypeConfig.getRequestHandlerClass());
      handler.handleRequest(user, startLocation.getAddress(), new LatLng(startLocation.getLat(), startLocation.getLng()),
        params.getComment(), cityId);
    }

    final List<String> driverTypes = Optional.ofNullable(params.getDriverType())
      .map(s -> s.split(","))
      .map(Arrays::asList)
      .orElse(Collections.emptyList());
    List<DriverType> requestedDriverTypes = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(driverTypes)) {
      for (String driverType : driverTypes) {
        DriverType requestedDriverType = driverTypeService.getOne(driverType);
        if (requestedDriverType == null) {
          throw new ServerError(String.format("Driver type %s not found", driverType));
        }
        requestedDriverTypes.add(requestedDriverType);
      }
      eligibilityCheckService.check(new RiderEligibilityCheckContext(rider, ImmutableMap.of(
        RiderEligibilityCheckContext.CITY, cityId,
        RiderEligibilityCheckContext.CAR_CATEGORY, requestedCarType,
        RiderEligibilityCheckContext.DRIVER_TYPE, requestedDriverTypes
      )), cityId);
    }

    final String applePayToken = params.getApplePayToken();
    Ride ride = new Ride();
    ride.setCityId(cityId);
    ride.setStatus(RideStatus.REQUESTED);
    ride.setRider(rider);
    ride.setRiderSession(riderSession);
    final int requestedDriverTypeBitmask = requestedDriverTypes.stream().mapToInt(DriverType::getBitmask).reduce(0, (i, j) -> i | j);
    ride.setRequestedDriverTypeBitmask(requestedDriverTypeBitmask);
    ride.setRequestedOn(new Date());
    ride.setApplePayToken(applePayToken);
    ride.setComment(params.getComment());

    rideService.fillStartLocation(ride, startLocation);
    rideService.fillEndLocation(ride, endLocation, false);

    ride.setRequestedCarType(requestedCarType);

    Optional<RedisSurgeArea> startArea = surgePricingService.getSurgeAreaByCarType(ride.getStartLocationLat(), ride.getStartLocationLong(), ride.getCityId(), requestedCarType);
    ride.setStartAreaId(startArea.map(sa -> sa.getAreaGeometry().getId()).orElse(null));

    if (!inSurgeArea && surgePricingService.isSurgeMandatory(ride.getStartLocationLat(),
      ride.getStartLocationLong(), requestedCarType, ride.getCityId())) {
      throw new BadRequestException("Priority Fare is active - please request again");
    }

    if (inSurgeArea) {
      ride.setSurgeFactor(surgePricingService.getSurgeFactor(new LatLng(ride.getStartLocationLat(),
        ride.getStartLocationLong()), ride.getRequestedCarType(), ride.getCityId()));
    }

    ride.setFareDetails(fareEstimateService.estimateFare(ride).orElse(new FareDetails()));
    if (!paymentConfig.isAsyncPreauthEnabled()) {
      final String prechargeId = preauthorizationService.preauthorizeRide(ride, carTypeConfig, applePayToken, () -> {
        //do nothing
      });
      ride.setPreChargeId(prechargeId);
    }

    Ride savedRide = rideDslRepository.save(ride);
    if (rider.isDispatcherAccount() && params.isRiderOverridden()) {
      RiderOverride riderOverride = new RiderOverride(
        Optional.ofNullable(params.getRiderFirstName()).orElse(rider.getFirstname()),
        Optional.ofNullable(params.getRiderLastName()).orElse(rider.getLastname()),
        Optional.ofNullable(params.getRiderPhoneNumber()).orElse(rider.getPhoneNumber()),
        rider, savedRide);
      savedRide.setRiderOverride(riderOverride);
      rideDslRepository.saveAny(riderOverride);
      savedRide = rideDslRepository.save(savedRide);
    }

    // Queue the request to dispatch the ride

    paymentJobService.afterCommit(() -> {
      final String directConnectId = params.getDirectConnectId();
      dispatchInfo(log, ride.getId(), String.format("Initiating dispatch. %s", directConnectId == null ? "Search will be performed by distance" : String.format("Search will be performed by DCID %s", directConnectId)));
      dispatchInfo(log, ride.getId(), String.format("[RA15117] Received CT %s. Assigned CT is %s", carCategory, ride.getRequestedCarType().getCarCategory()));
      RideRequestContext requestContext = StateMachineUtils.createRequestContext(ride, dispatchConfig.getDriverSearchRadiusStart(), directConnectId);
      StateMachine<States, Events> machine = machineProvider.createMachine(requestContext);
      machine.start();
    });
    return savedRide;
  }

  @Transactional
  public RideQueueToken requestRide(RideStartLocation startLocation, RideEndLocation endLocation, String carCategory, Long cityId) throws RideAustinException {
    CarType requestedCarType = carTypesCache.getCarType(carCategory);
    Ride ride = new Ride();
    ride.setCityId(cityId);
    ride.setStatus(RideStatus.REQUEST_QUEUED);
    ride.setRequestedOn(new Date());
    ride.setRequestedCarType(requestedCarType);

    rideService.fillStartLocation(ride, startLocation);
    rideService.fillEndLocation(ride, endLocation, false);

    Optional<RedisSurgeArea> startArea = surgePricingService.getSurgeAreaByCarType(ride.getStartLocationLat(), ride.getStartLocationLong(), ride.getCityId(), requestedCarType);
    ride.setStartAreaId(startArea.map(sa -> sa.getAreaGeometry().getId()).orElse(null));

    if (ride.getStartAreaId() != null) {
      ride.setSurgeFactor(surgePricingService.getSurgeFactor(new LatLng(ride.getStartLocationLat(),
        ride.getStartLocationLong()), ride.getRequestedCarType(), ride.getCityId()));
    }

    ride.setFareDetails(fareEstimateService.estimateFare(ride).orElse(new FareDetails()));

    Ride savedRide = rideDslRepository.save(ride);

    return new RideQueueToken(savedRide.getId(), UUID.nameUUIDFromBytes(StateMachineUtils.getMachineId(environment, savedRide.getId()).getBytes()).toString(),
    Date.from(Instant.now().plus(dispatchConfig.getRideQueueExpirationTimeout(), ChronoUnit.SECONDS)));
  }

  public MobileRiderRideDto associateRide(String token) throws RideAustinException {
    final RideQueueToken queueToken = rideQueueTokenDslRepository.findOne(token);
    if (queueToken == null) {
      throw new NotFoundException("Ride request not found");
    }
    if (queueToken.isExpired()) {
      throw new BadRequestException("Ride request has already expired, please try again");
    }
    queueToken.setExpired(true);
    rideQueueTokenDslRepository.save(queueToken);
    Ride requestedRide = rideDslRepository.findOne(queueToken.getRideId());
    final User user = currentUserService.getUser();
    final Rider rider = user.getAvatar(Rider.class);
    requestedRide.setRider(rider);
    requestedRide.setStatus(RideStatus.REQUESTED);
    requestedRide.setRider(rider);
    requestedRide.setRiderSession(currentSessionService.getCurrentSession(user));
    requestedRide.setRequestedOn(new Date());
    final RideRequestContext requestContext = StateMachineUtils.createRequestContext(requestedRide, dispatchConfig.getDriverSearchRadiusStart());
    requestedRide = rideDslRepository.save(requestedRide);
    StateMachine<States, Events> machine = machineProvider.createMachine(requestContext);
    machine.start();
    return rideDslRepository.findRiderRideInfo(requestedRide.getId());
  }

  public void acknowledgeHandshake(Long id, DeferredResult<ResponseEntity<Object>> result) {
    User user = currentUserService.getUser();
    sendEvent(id, Events.HANDSHAKE_ACKNOWLEDGE, new DispatchReachMessage(result, user.getId()));
  }

  public void acceptRide(Long id, DeferredResult<ResponseEntity<Object>> result) {
    User user = currentUserService.getUser();
    ActiveDriver activeDriver = activeDriversService.getActiveDriverByDriver(user);
    if (activeDriver != null && requestedDriversRegistry.isRequested(activeDriver.getId())) {
      sendEvent(id, Events.DISPATCH_REQUEST_ACCEPT, new RideAcceptMessage(result, user.getId()));
    } else {
      flowInfo(log, id, String.format("User %d tried to accept ride after dispatch expired", user.getId()));
      result.setResult(ResponseEntity.badRequest().body("Ride is already redispatched, you can't accept it"));
    }
  }

  public void cancelAsAdmin(long rideId) {
    sendEvent(rideId, Events.ADMIN_CANCEL, new MessageHeaders(Collections.emptyMap()));
  }

  public void cancelAsDriver(long rideId, DeferredResult<ResponseEntity<Object>> result, CancellationReason reason, String comment) {
    if (reason != null) {
      try {
        cancellationFeedbackService.submit(rideId, reason, AvatarType.DRIVER, comment);
      } catch (BadRequestException e) {
        if (!result.hasResult()) {
          result.setErrorResult(e);
        }
      }
    }
    sendEvent(rideId, Events.DRIVER_CANCEL, new DeferredResultMessage<>(result));
  }

  public void cancelAsRider(long rideId, DeferredResult<ResponseEntity<Object>> result) {
    sendEvent(rideId, Events.RIDER_CANCEL, new DeferredResultMessage<>(result));
  }

  @CacheEvict(cacheNames = {CacheConfiguration.ETC_CACHE}, key = "#p0", cacheManager = "etcCacheManager")
  public void updateDestination(long rideId, RideEndLocation endLocation, DeferredResult<ResponseEntity> result) {
    long userId = currentUserService.getUser().getId();
    sendEvent(rideId, Events.UPDATE_DESTINATION, new UpdateDestinationMessage(result, userId, endLocation));
  }

  public void updateComment(long rideId, String comment) {
    long userId = currentUserService.getUser().getId();
    sendEvent(rideId, Events.UPDATE_COMMENT, new UpdateCommentMessage(userId, comment));
  }

  public void declineRide(Long rideId) {
    long userId = currentUserService.getUser().getId();
    sendEvent(rideId, Events.DISPATCH_REQUEST_DECLINE, new DeclineDispatchMessage(userId));
  }

  public void driverReached(long id, DeferredResult<ResponseEntity<Object>> result) {
    sendEvent(id, Events.DRIVER_REACH, new DriverReachMessage(result));
  }

  @CacheEvict(cacheNames = {CacheConfiguration.ETC_CACHE}, key = "#p0", cacheManager = "etcCacheManager")
  public void startRide(long id, DeferredResult<ResponseEntity<Object>> result) {
    sendEvent(id, Events.START_RIDE, new RideStartMessage(result));
  }

  public DeferredResult<MobileDriverRideDto> endRide(Long rideId, RideEndLocation endLocation, DeferredResult<MobileDriverRideDto> deferredResult) {
    sendEvent(rideId, Events.END_RIDE, new EndRideMessage(endLocation, deferredResult));
    return deferredResult;
  }

  private void sendEvent(long rideId, Events event, MessageHeaders headers) {
    String userId = Optional.ofNullable(currentUserService.getUser()).map(User::getId).map(String::valueOf).orElse("n/a");
    flowInfo(log, rideId, String.format("User %s sends %s", userId, event));
    if (event.isProxiedToInception()) {
      machineProvider.sendProxiedEvent(rideId, event, headers);
      return;
    }
    Optional<StateMachine<States, Events>> restored = machineProvider.restoreMachine(rideId, event, headers);
    if (restored.isPresent() && !restored.get().getExtendedState().getVariables().isEmpty()) {
      StateMachine<States, Events> machine = restored.get();
      if (handleDestinationUpdate(event, headers, machine)) {
        return;
      }
      boolean eventSent = machine.sendEvent(new GenericMessage<>(event, headers));
      if (eventSent) {
        flowInfo(log, rideId, String.format("Event %s successfully sent", event));
      } else {
        log.error(String.format("[Ride #%d] Failed to send event %s, current state %s. Context: %s", rideId, event, machine.getState().getId(), machine.getExtendedState()));
        if (headers instanceof DeferredResultMessage && ((DeferredResultMessage) headers).getDeferredResult() != null) {
          errorProvider.errorResultSetter(machine.getState().getId(), event)
            .accept(((DeferredResultMessage) headers).getDeferredResult());
        }
      }
    } else {
      log.error(String.format("Failed to restore machine #%d from storage", rideId));
      if (headers instanceof DeferredResultMessage) {
        ((DeferredResultMessage) headers).getDeferredResult().setErrorResult(new BadRequestException("Ride state is not found"));
      }
    }
  }

  private boolean handleDestinationUpdate(Events event, MessageHeaders messageHeaders, StateMachine<States, Events> machine) {
    if (event == Events.UPDATE_DESTINATION) {
      final DeferredResultMessage headers = (DeferredResultMessage) messageHeaders;
      final RideFlowContext flowContext = StateMachineUtils.getFlowContext(machine.getExtendedState());
      if (destinationUpdateConfig.isDestinationUpdateLimited() && flowContext.getStartedOn() != null
        && flowContext.getDestinationUpdates() == destinationUpdateConfig.getDestinationUpdateLimit()) {
        final String message = String.format("You can't change your destination more than %d times while in a ride. Please order another ride", destinationUpdateConfig.getDestinationUpdateLimit());
        headers.getDeferredResult().setErrorResult(new BadRequestException(message));
        return true;
      } else {
        headers.getDeferredResult().setResult(ResponseEntity.ok().build());
      }
    }
    return false;
  }
}
