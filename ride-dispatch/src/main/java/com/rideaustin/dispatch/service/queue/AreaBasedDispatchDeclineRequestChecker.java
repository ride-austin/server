package com.rideaustin.dispatch.service.queue;

import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.google.maps.model.LatLng;
import com.rideaustin.dispatch.service.DispatchDeclineRequestChecker;
import com.rideaustin.model.Area;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.service.areaqueue.AreaService;
import com.rideaustin.service.location.ObjectLocationService;
import com.rideaustin.service.location.enums.LocationType;
import com.rideaustin.service.model.OnlineDriverDto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AreaBasedDispatchDeclineRequestChecker implements DispatchDeclineRequestChecker<Optional<AreaBasedDispatchDeclineRequestChecker.AreaBasedDispatchDeclineActiveDriverData>> {

  private final AreaService areaService;
  private final ObjectLocationService<OnlineDriverDto> locationService;

  public boolean checkIfActiveDriverNeedAddConsecutiveDeclineRequest(Optional<AreaBasedDispatchDeclineActiveDriverData> activeDriverData, Ride ride) {
    if (activeDriverData.isPresent()) {
      // if driver is in area and request is not from outside area
      Area currentRequestArea = areaService.isInArea(ride);
      Area currentActiveDriverArea = areaService.isInArea(activeDriverData.get().position, ride.getCityId());
      boolean rideIsInArea = currentRequestArea != null;
      boolean activeDriverIsInArea = currentActiveDriverArea != null;
      if (!rideIsInArea && activeDriverIsInArea) {
        return false;
      }
      return !(rideIsInArea && activeDriverIsInArea && (currentRequestArea.getId() != currentActiveDriverArea.getId()));
    }
    return false;
  }

  @Override
  public Optional<AreaBasedDispatchDeclineActiveDriverData> createActiveDriverData(long activeDriverId) {
    return Optional.ofNullable(locationService.getById(activeDriverId, LocationType.ACTIVE_DRIVER))
      .map(lo -> new LatLng(lo.getLatitude(), lo.getLongitude()))
      .map(AreaBasedDispatchDeclineActiveDriverData::new);
  }

  @Getter
  @RequiredArgsConstructor
  public static class AreaBasedDispatchDeclineActiveDriverData {
    private final LatLng position;
  }

}