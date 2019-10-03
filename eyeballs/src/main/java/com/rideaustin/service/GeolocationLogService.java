package com.rideaustin.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableList;
import com.rideaustin.model.GeolocationLog;
import com.rideaustin.model.enums.GeolocationLogEvent;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.user.Rider;
import com.rideaustin.repo.dsl.GeolocationLogDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.repo.dsl.RiderDslRepository;
import com.rideaustin.rest.exception.ForbiddenException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class GeolocationLogService {

  private final GeolocationLogDslRepository geolocationLogRepo;
  private final CurrentUserService cuSvc;
  private final RideDslRepository rideDslRepository;
  private final RiderDslRepository riderDslRepository;

  public GeolocationLog addGeolocationLog(@Nonnull Double locationLat, @Nonnull Double locationLng,
    @Nonnull GeolocationLogEvent event, @Nonnull Long riderId) throws ForbiddenException {
    return addGeolocationLog(locationLat, locationLng, event, riderId, null);
  }

  public GeolocationLog addGeolocationLog(@Nonnull Double locationLat, @Nonnull Double locationLng,
    @Nonnull GeolocationLogEvent event, @Nonnull Long riderId, CarType carType) throws ForbiddenException {
    GeolocationLog log = new GeolocationLog();

    Rider rider = riderDslRepository.getRider(riderId);

    checkIfAuthenticated(rider);
    boolean shouldStoreEvent = shouldStoreEvent(rider, event);
    if (shouldStoreEvent) {
      log.setEvent(event);
      log.setLocationLat(locationLat);
      log.setLocationLng(locationLng);
      log.setRider(rider);
      if (carType != null) {
        log.setCarType(carType);
      }
      try {
        log = geolocationLogRepo.save(log);
      } catch (Exception e) {
        GeolocationLogService.log.error(String.format("Unable to log - lat: %s, long: %s, event: %s, riderId: %d",
          locationLat, locationLng, event.name(), riderId), e);
      }
    }
    return log;
  }

  public List<GeolocationLog> findBetweenDates(@Nonnull Date beginDate, @Nonnull Date endDate) {
    List<GeolocationLog> plainList = geolocationLogRepo.findBetweenDatesWithEvent(beginDate, endDate, GeolocationLogEvent.GET_ACTIVE_DRIVERS_BY_RIDER);
    Map<String, GeolocationLog> distinct = new HashMap<>();
    for (GeolocationLog log : plainList) {
      distinct.putIfAbsent(getGeoLogMapKey(log), log);
    }
    return ImmutableList.copyOf(distinct.values());
  }

  private String getGeoLogMapKey(GeolocationLog log) {
    Optional<GeolocationLog> optionalLog = Optional.ofNullable(log);
    return optionalLog.map(GeolocationLog::getRider).map(Rider::getId).orElse(0L) + "_" + optionalLog.map(GeolocationLog::getCarType).map(CarType::getCarCategory).orElse("");
  }

  private boolean shouldStoreEvent(Rider rider, GeolocationLogEvent event) {
    return GeolocationLogEvent.RIDER_APP_OPEN != event || !Optional.ofNullable(rideDslRepository.isInRide(rider)).orElse(false);
  }

  private void checkIfAuthenticated(Rider rider) throws ForbiddenException {
    if (!rider.getUser().equals(cuSvc.getUser())) {
      throw new ForbiddenException();
    }
  }

}
