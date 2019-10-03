package com.rideaustin.dispatch.actions;

import static com.rideaustin.dispatch.LogUtil.dispatchInfo;

import java.util.Date;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.statemachine.StateContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.dispatch.aop.DeferredResultAction;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.enums.DispatchStatus;
import com.rideaustin.model.enums.EventType;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.CityDriverType;
import com.rideaustin.model.ride.CityDriverType.Configuration;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.dsl.ActiveDriverDslRepository;
import com.rideaustin.repo.dsl.RideDriverDispatchDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.service.ActiveDriverLocationService;
import com.rideaustin.service.event.EventsNotificationService;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.service.model.context.RideFlowContext;
import com.rideaustin.service.model.context.RideRequestContext;
import com.rideaustin.service.notification.RideFlowPushNotificationFacade;
import com.rideaustin.service.ride.RideLoadService;
import com.rideaustin.service.user.DriverTypeCache;
import com.rideaustin.utils.dispatch.StateMachineUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RedispatchOnCancelAction extends ReleaseRequestedDriversAction {

  @Inject
  private RideLoadService rideLoadService;
  @Inject
  protected RideDslRepository rideDslRepository;
  @Inject
  protected RideDriverDispatchDslRepository rideDriverDispatchDslRepository;
  @Inject
  private ActiveDriverDslRepository activeDriverDslRepository;
  @Inject
  protected ActiveDriverLocationService activeDriverLocationService;
  @Inject
  protected EventsNotificationService eventsNotificationService;
  @Inject
  private RideFlowPushNotificationFacade pushNotificationsFacade;
  @Inject
  private DriverTypeCache driverTypeCache;
  @Inject
  private ObjectMapper objectMapper;

  @Override
  @DeferredResultAction
  public void execute(StateContext<States, Events> context) {
    DispatchContext dispatchContext = StateMachineUtils.getDispatchContext(context);
    RideRequestContext requestContext = StateMachineUtils.getRequestContext(context);
    dispatchInfo(log, requestContext, "Preparing ride for redispatch after driver cancel");

    Ride lockedRide = updateRide(dispatchContext);

    updateDispatchHistory(dispatchContext);
    sendNotifications(dispatchContext, requestContext, lockedRide);
    updateActiveDriver(dispatchContext);
    updateContexts(context, requestContext);

    super.execute(context);
  }

  protected void sendNotifications(DispatchContext dispatchContext, RideRequestContext requestContext, Ride lockedRide) {
    //notifications
    if (requestContext.getDirectConnectId() == null) {
      pushNotificationsFacade.pushRideRedispatchNotification(dispatchContext.getId());
    }

    if (dispatchContext.getCandidate() != null) {
      eventsNotificationService.sendRideUpdateToDriver(lockedRide, dispatchContext.getCandidate(), EventType.DRIVER_CANCELLED);
    }
  }

  protected void updateActiveDriver(DispatchContext dispatchContext) {
    if (dispatchContext.getCandidate() != null) {
      long activeDriverId = dispatchContext.getCandidate().getId();
      if (rideDslRepository.findPrecedingRide(activeDriverId) == null) {
        activeDriverLocationService.updateActiveDriverLocationStatus(activeDriverId, ActiveDriverStatus.AVAILABLE);
        activeDriverDslRepository.setRidingDriverAsAvailable(activeDriverId);
      } else {
        stackedDriverRegistry.makeStackable(activeDriverId);
      }
    }
  }

  protected void updateContexts(StateContext<States, Events> context, RideRequestContext requestContext) {
    if (requestContext.getDirectConnectId() != null) {
      requestContext.setDirectConnectId(null);
      requestContext.setRequestedDriverTypeBitmask(null);
    }
    requestContext.setCreatedDate(new Date());
    StateMachineUtils.updateRequestContext(context, requestContext, persister, environment);
    RideFlowContext flowContext = StateMachineUtils.getFlowContext(context);
    flowContext.setAcceptedOn(null);
    StateMachineUtils.updateFlowContext(context, flowContext, persister, environment);
  }

  protected void updateDispatchHistory(DispatchContext dispatchContext) {
    if (dispatchContext.getCandidate() != null) {
      long activeDriverId = dispatchContext.getCandidate().getId();
      rideDriverDispatchDslRepository.declineRequest(dispatchContext.getId(), activeDriverId, DispatchStatus.CANCELLED);
    }
  }

  protected Ride updateRide(DispatchContext dispatchContext) {
    Ride lockedRide = rideLoadService.findOneForUpdateWithRetry(dispatchContext.getId());
    lockedRide.setStatus(RideStatus.REQUESTED);
    lockedRide.setActiveDriver(null);
    lockedRide.setDriverAcceptedOn(null);
    lockedRide.setRequestedOn(new Date());
    Integer requestedDriverTypeBitmask = lockedRide.getRequestedDriverTypeBitmask();
    final Set<CityDriverType> driverTypes = driverTypeCache.getByCityAndBitmask(lockedRide.getCityId(), requestedDriverTypeBitmask);
    for (CityDriverType driverType : driverTypes) {
      final Configuration driverTypeConfiguration = driverType.getConfigurationObject(objectMapper);
      if (driverTypeConfiguration.shouldResetOnRedispatch()) {
        requestedDriverTypeBitmask ^= driverType.getBitmask();
        lockedRide.setRequestedDriverTypeBitmask(requestedDriverTypeBitmask);
      }
    }
    rideDslRepository.save(lockedRide);
    return lockedRide;
  }

}
