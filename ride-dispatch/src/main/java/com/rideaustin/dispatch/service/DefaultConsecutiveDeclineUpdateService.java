package com.rideaustin.dispatch.service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;

import com.google.common.collect.ImmutableSet;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.ActiveDriverLocationService;
import com.rideaustin.service.ActiveDriversService;
import com.rideaustin.service.config.RideDispatchServiceConfig;
import com.rideaustin.service.event.EventsNotificationService;
import com.rideaustin.service.location.enums.LocationType;
import com.rideaustin.service.model.ConsecutiveDeclinedRequestsData;
import com.rideaustin.service.model.DispatchRequest;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.service.user.CarTypesUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DefaultConsecutiveDeclineUpdateService implements ConsecutiveDeclineUpdateService {

  private final ActiveDriverLocationService activeDriverLocationService;
  private final EventsNotificationService eventsNotificationService;
  private final ActiveDriversService activeDriversService;
  private final RideDispatchServiceConfig config;
  private final List<DispatchDeclineRequestChecker> checkers;

  @Override
  public void processDriverDecline(Ride ride, DispatchRequest request) {
    try {
      boolean shouldDeactivateCategory = processConsecutiveRideDecline(request, ride, checkers);
      if (shouldDeactivateCategory) {
        Integer availableCategories = activeDriversService.disableCarCategoryInActiveDriver(request.getActiveDriverId(), ride.getRequestedCarType().getBitmask());
        eventsNotificationService.sendCarCategoryChange(request.getDriverId(), ImmutableSet.of(ride.getRequestedCarType().getCarCategory()));
        if (availableCategories == 0) {
          activeDriversService.deactivateWithMessage(request.getActiveDriverId(), request.getDriverId(), ActiveDriversService.GoOfflineEventSource.MISSED_RIDES);
        }
      }
    } catch (ServerError e) {
      log.error("Error occurred", e);
    }
  }

  protected boolean processConsecutiveRideDecline(DispatchRequest request, Ride ride, Collection<DispatchDeclineRequestChecker> checkers) {
    if (CollectionUtils.isNotEmpty(checkers)) {
      for (DispatchDeclineRequestChecker checker : checkers) {
        if (!checker.checkIfActiveDriverNeedAddConsecutiveDeclineRequest(checker.createActiveDriverData(request.getActiveDriverId()), ride)) {
          return false;
        }
      }
    }
    OnlineDriverDto activeDriver = activeDriverLocationService.getById(request.getActiveDriverId(), LocationType.ACTIVE_DRIVER);
    if (activeDriver == null) {
      return false;
    }
    ConsecutiveDeclinedRequestsData declineData = Optional.ofNullable(activeDriver.getConsecutiveDeclinedRequests())
      .orElseGet(() -> new ConsecutiveDeclinedRequestsData(CarTypesUtils.fromBitMask(activeDriver.getAvailableCarCategoriesBitmask())));
    Integer declineCount = declineData.increase(ride.getRequestedCarType().getCarCategory());
    activeDriver.setConsecutiveDeclinedRequests(declineData);
    activeDriverLocationService.saveOrUpdateLocationObject(activeDriver);
    return shouldDeactivate(declineCount);
  }

  protected boolean shouldDeactivate(Integer declineCount) {
    return declineCount == config.getDriverMaxDeclinedRequests();
  }

}
