package com.rideaustin.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.google.maps.model.LatLng;
import com.rideaustin.model.DistanceAware;
import com.rideaustin.model.DrivingTimeAware;
import com.rideaustin.model.LocationAware;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.service.config.StackedRidesConfig;
import com.rideaustin.service.location.model.LocationObject;
import com.rideaustin.service.model.ETCCalculationInfo;
import com.rideaustin.utils.map.MapUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class UpdateDistanceTimeService {

  private final MapService mapService;
  private final RideDslRepository rideDslRepository;
  private final StackedRidesConfig config;

  public <T extends DistanceAware & LocationAware & DrivingTimeAware> void updateDistanceTime(@Nonnull Double latitude, @Nonnull Double longitude, List<T> result, Integer maxActiveDriverAvatars, boolean addExpectationTime) {
    if (maxActiveDriverAvatars > 0) {
      MapUtils.updateDistanceToRider(latitude, longitude, result);
      List<T> driversToCalculateETA = new ArrayList<>(result.subList(0, Math.min(maxActiveDriverAvatars, result.size())));
      updateTimeToDrive(latitude, longitude, driversToCalculateETA, addExpectationTime);
    }
  }

  public <T extends DistanceAware & LocationAware & DrivingTimeAware> void updateDistanceTimeWithETC(@Nonnull Double latitude,
    @Nonnull Double longitude, List<T> result, Integer maxActiveDriverAvatars, Predicate<T> etcFilterPredicate) {
    updateDistanceTime(latitude, longitude, result, maxActiveDriverAvatars, false);
    int dropoffExpectation = config.getStackingDropoffExpectation();
    for (int i = 0; i < Math.min(maxActiveDriverAvatars, result.size()); i++) {
      T driverDto = result.get(i);
      if (etcFilterPredicate.test(driverDto)) {
        ETCCalculationInfo etcInfo = rideDslRepository.getETCCalculationInfoForDriver(driverDto.getId());
        if (etcInfo != null) {
          Long etc = Optional.ofNullable(mapService.getTimeToDriveCached(etcInfo.getRideId(), new LatLng(driverDto.getLatitude(), driverDto.getLongitude()),
            new LatLng(etcInfo.getEndLat(), etcInfo.getEndLng()))).orElse(60L);
          long ecd = (long) MapUtils.calculateDirectDistance(driverDto.getLongitude(), driverDto.getLatitude(), etcInfo.getEndLng(), etcInfo.getEndLat());
          etcInfo.setLocationObject(new LocationObject(etcInfo.getEndLat(), etcInfo.getEndLng()));
          updateDistanceTime(latitude, longitude, Collections.singletonList(etcInfo), 1, false);
          Long dropoffETA = Optional.ofNullable(etcInfo.getDrivingTimeToRider()).orElse(60L);
          Long dropoffDistance = Optional.ofNullable(etcInfo.getDrivingDistanceToRider()).orElse(0L);
          driverDto.setDrivingTimeToRider(etc + dropoffETA + dropoffExpectation);
          driverDto.setDrivingDistanceToRider(ecd + dropoffDistance);
        }
      }
    }
    sort(result);
  }

  public long getDrivingTimeWithETC(long rideId, long activeDriverId, Double fromLat, Double fromLng, double toLat, double toLng) {
    long result = Optional.ofNullable(mapService.getTimeToDriveCached(rideId, new LatLng(fromLat, fromLng), new LatLng(toLat, toLng))).orElse(0L);
    ETCCalculationInfo etcInfo = rideDslRepository.getETCCalculationInfoForDriver(activeDriverId);
    if (etcInfo != null) {
      int dropoffExpectation = config.getStackingDropoffExpectation();
      long etc = Optional.ofNullable(mapService.getTimeToDriveCached(etcInfo.getRideId(), new LatLng(toLat, toLng),
        new LatLng(etcInfo.getEndLat(), etcInfo.getEndLng()))).orElse(0L);
      result += dropoffExpectation + etc;
    }
    return result;
  }

  private <T extends LocationAware & DistanceAware & DrivingTimeAware> void updateTimeToDrive(@Nonnull Double latitude, @Nonnull Double longitude, List<T> activeDrivers, boolean addExpectationTime) {
    mapService.updateTimeToDrive(activeDrivers, latitude, longitude, addExpectationTime);
    //comparator that moves null values to the end
    sort(activeDrivers);
  }

  private <T extends DrivingTimeAware> void sort(List<T> activeDrivers) {
    activeDrivers.sort((ad1, ad2) -> {
      if (ad1.getDrivingTimeToRider() == null) {
        return (ad2.getDrivingTimeToRider() == null) ? 0 : 1;
      }
      if (ad2.getDrivingTimeToRider() == null) {
        return -1;
      }
      return ad1.getDrivingTimeToRider().compareTo(ad2.getDrivingTimeToRider());
    });
  }
}
