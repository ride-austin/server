package com.rideaustin.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.maps.model.LatLng;
import com.rideaustin.Constants;
import com.rideaustin.model.Area;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.rest.model.CompactActiveDriverDto;
import com.rideaustin.service.areaqueue.AreaService;
import com.rideaustin.service.config.ActiveDriverServiceConfig;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.service.user.DriverTypeUtils;
import com.rideaustin.utils.map.MapUtils;
import com.sromku.polygon.Point;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ActiveDriverSearchService {

  private static final long LONG_TIME_STUB = 10000L;

  private final ActiveDriverServiceConfig config;
  private final ActiveDriverLocationService activeDriverLocationService;
  private final AreaService areaService;
  private final UpdateDistanceTimeService updateDistanceTimeService;
  private final RequestedDriversRegistry requestedDriversRegistry;
  private final DefaultSearchDriverHandler defaultSearchDriverHandler;

  public List<CompactActiveDriverDto> findActiveDriversForDriver(Long cityId, @Nonnull Double latitude, @Nonnull Double longitude) {

    List<OnlineDriverDto> activeDrivers = activeDriverLocationService.locationAround(latitude, longitude, Constants.SQUARE_MILE_DIMENSION_MILES, null, null);

    if (cityId != null) {
      activeDrivers = activeDrivers.stream()
        .filter(Objects::nonNull)
        .filter(ad -> cityId.equals(ad.getCityId()))
        .collect(Collectors.toList());
    }
    MapUtils.updateDistanceToRider(latitude, longitude, activeDrivers);

    activeDrivers = activeDrivers.stream().limit(config.getMaxActiveDriverAvatars()).collect(Collectors.toList());
    return convertToCompactDto(activeDrivers);
  }

  public List<CompactActiveDriverDto> findAvailableActiveDriversForRider(@Nonnull Double latitude, @Nonnull Double longitude,
    CarType carType, String driverType, Long cityId) {
    List<OnlineDriverDto> result;
    Integer driverTypeBitmask = DriverTypeUtils.toBitMask(driverType);
    if (carType != null) {
      Area queuedArea = areaService.isInArea(new LatLng(latitude, longitude), cityId);
      if (queuedArea != null) {
        result = defaultSearchDriverHandler.searchDrivers(new QueuedActiveDriverSearchCriteria(queuedArea, Collections.emptyList(),
          carType.getCarCategory(), driverTypeBitmask));
        updateDistanceTimeService.updateDistanceTime(latitude, longitude, result, config.getMaxActiveDriverAvatars(), true);
      } else {
        result = defaultSearchDriverHandler.searchDrivers(
          new ActiveDriverSearchCriteria(latitude, longitude, null, config.getMaxActiveDriverAvatars(),
            carType.getCarCategory(), carType.getBitmask(), cityId, driverTypeBitmask, Constants.SQUARE_MILE_DIMENSION_MILES,
            config.getMaxActiveDriverAvatars()))
          .stream()
          .filter(this::shouldDisplayBasedOnEta)
          .peek(ad -> {
            //populate time to driver as in apps that will be interpreted as 0
            if (ad.getDrivingTimeToRider() == null) {
              ad.setDrivingTimeToRider(LONG_TIME_STUB);
            }
          })
          .limit(config.getMaxActiveDriverAvatars())
          .collect(Collectors.toList());
      }
    } else {
      result = Lists.newArrayList();
    }
    result = result.stream().filter(ad -> !requestedDriversRegistry.isRequested(ad.getId())).collect(Collectors.toList());
    return convertToCompactDto(result);
  }

  private List<CompactActiveDriverDto> convertToCompactDto(List<OnlineDriverDto> result) {
    return result.stream()
      .map(o -> {
        CompactActiveDriverDto compactActiveDriverDto = new CompactActiveDriverDto(o.getId(), o.getDriverId(), o.getUserId(), o.getStatus());
        compactActiveDriverDto.setLocation(o.getLocationObject());
        boolean requested = requestedDriversRegistry.isRequested(o.getId());
        if (requested) {
          compactActiveDriverDto.setStatus(ActiveDriverStatus.REQUESTED);
        }
        compactActiveDriverDto.setDrivingTimeToRider(o.getDrivingTimeToRider());
        return compactActiveDriverDto;
      })
      .collect(Collectors.toList());
  }

  private boolean shouldDisplayBasedOnEta(OnlineDriverDto ad) {
    if (ad.getDrivingTimeToRider() != null) {
      if (config.getCityCenterDispatchPolygon().contains(new Point((float) ad.getLatitude(), (float) ad.getLongitude()))) {
        return ad.getDrivingTimeToRider() <= config.getDriverCityCenterMaxEtaTime();
      } else {
        return ad.getDrivingTimeToRider() <= config.getDriverMaxEtaTime();
      }
    }
    //if we don't know eta we will display anyway
    return true;
  }

}
