package com.rideaustin.dispatch.service;

import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.ride.CityDriverType;
import com.rideaustin.model.ride.CityDriverType.Configuration;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.service.location.ObjectLocationService;
import com.rideaustin.service.location.enums.LocationType;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.service.user.DriverTypeCache;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DriverTypeDispatchDeclineRequestChecker implements DispatchDeclineRequestChecker<DriverTypeDispatchDeclineRequestChecker.DriverTypeDispatchDeclineActiveDriverData> {

  private final ObjectMapper objectMapper;
  private final DriverTypeCache driverTypeCache;
  private final ObjectLocationService<OnlineDriverDto> locationService;

  public boolean checkIfActiveDriverNeedAddConsecutiveDeclineRequest(DriverTypeDispatchDeclineActiveDriverData activeDriverData, Ride ride) {
    if (!activeDriverData.getStatus().equals(ActiveDriverStatus.AVAILABLE)) {
      return false;
    }
    Integer requestedDriverType = ride.getRequestedDriverTypeBitmask();
    final Set<CityDriverType> requestedCityDriverTypes;
    if (ride.getRequestedDriverTypeBitmask() != null) {
      requestedCityDriverTypes = driverTypeCache.getByCityAndBitmask(ride.getCityId(), ride.getRequestedDriverTypeBitmask());
    } else {
      return true;
    }
    if (requestedCityDriverTypes.isEmpty()) {
      return true;
    }
    for (CityDriverType cityDriverType : requestedCityDriverTypes) {
      if (requestedDriverType != null && cityDriverType.getConfiguration() != null) {
        Configuration configuration = cityDriverType.getConfigurationObject(objectMapper);
        if (!configuration.isPenalizeDeclinedRides()) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public DriverTypeDispatchDeclineActiveDriverData createActiveDriverData(long activeDriverId) {
    ActiveDriverStatus status = Optional.ofNullable(locationService.getById(activeDriverId, LocationType.ACTIVE_DRIVER))
      .map(OnlineDriverDto::getStatus)
      .orElse(ActiveDriverStatus.INACTIVE);
    return new DriverTypeDispatchDeclineActiveDriverData(status);
  }

  @Getter
  @RequiredArgsConstructor
  public static class DriverTypeDispatchDeclineActiveDriverData {
    private final ActiveDriverStatus status;
  }
}