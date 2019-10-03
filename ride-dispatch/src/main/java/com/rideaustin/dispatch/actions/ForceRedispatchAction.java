package com.rideaustin.dispatch.actions;

import javax.inject.Inject;

import org.springframework.statemachine.StateContext;

import com.google.maps.model.LatLng;
import com.rideaustin.dispatch.LogUtil;
import com.rideaustin.model.enums.DispatchStatus;
import com.rideaustin.model.enums.EventType;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.rest.model.MobileRiderRideDto.PrecedingRide;
import com.rideaustin.service.MapService;
import com.rideaustin.service.config.StackedRidesConfig;
import com.rideaustin.service.location.enums.LocationType;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.service.model.context.RideRequestContext;
import com.rideaustin.utils.dispatch.StateMachineUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ForceRedispatchAction extends RedispatchOnCancelAction {

  @Inject
  private MapService mapService;
  @Inject
  private StackedRidesConfig config;

  @Override
  public void execute(StateContext<States, Events> context) {
    RideRequestContext requestContext = StateMachineUtils.getRequestContext(context);
    DispatchContext dispatchContext = StateMachineUtils.getDispatchContext(context);
    long activeDriverId = dispatchContext.getCandidate().getId();
    LogUtil.dispatchInfo(log, requestContext, "Force redispatch stacked ride after destination update");
    super.execute(context);
    updateRequestContext(context, requestContext, activeDriverId);
  }

  @Override
  protected void updateDispatchHistory(DispatchContext dispatchContext) {
    if (dispatchContext.getCandidate() != null) {
      long activeDriverId = dispatchContext.getCandidate().getId();
      rideDriverDispatchDslRepository.declineRequest(dispatchContext.getId(), activeDriverId, DispatchStatus.REDISPATCHED);
    }
  }

  @Override
  protected void sendNotifications(DispatchContext dispatchContext, RideRequestContext requestContext, Ride lockedRide) {
    if (dispatchContext.getCandidate() != null) {
      eventsNotificationService.sendRideUpdateToDriver(lockedRide, dispatchContext.getCandidate(), EventType.RIDE_STACKED_REASSIGNED);
    }
  }

  @Override
  protected void updateActiveDriver(DispatchContext dispatchContext) {
    if (dispatchContext.getCandidate() != null) {
      long activeDriverId = dispatchContext.getCandidate().getId();
      OnlineDriverDto onlineDriverDto = activeDriverLocationService.getById(activeDriverId, LocationType.ACTIVE_DRIVER);
      PrecedingRide precedingRide = rideDslRepository.findPrecedingRide(activeDriverId);
      Long newETC = mapService.getTimeToDriveCached(precedingRide.getId(), new LatLng(onlineDriverDto.getLatitude(), onlineDriverDto.getLongitude()),
        new LatLng(precedingRide.getEnd().getLatitude(), precedingRide.getEnd().getLongitude()));
      onlineDriverDto.setEligibleForStacking(newETC < config.getEndRideTimeThreshold(onlineDriverDto.getCityId()));
      if (onlineDriverDto.isEligibleForStacking()) {
        stackedDriverRegistry.makeStackable(activeDriverId);
      } else {
        stackedDriverRegistry.removeFromStack(activeDriverId);
      }
      activeDriverLocationService.saveOrUpdateLocationObject(onlineDriverDto);
    }
  }

  private void updateRequestContext(StateContext<States, Events> context, RideRequestContext requestContext, long activeDriverId) {
    requestContext.getIgnoreIds().remove(activeDriverId);
    super.updateContexts(context, requestContext);
  }
}
