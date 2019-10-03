package com.rideaustin.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.maps.model.LatLng;
import com.rideaustin.Constants;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.ride.RideTracker;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.repo.dsl.RideTrackerDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.service.generic.TimeService;
import com.rideaustin.service.thirdparty.S3StorageService;
import com.rideaustin.utils.map.MapUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RideTrackerService {

  private static final double DISTANCE_500M = 500;
  private static final long TEN_SECONDS_IN_MILLIS = 10000;
  private static final double STUB_TIME_BETWEEN_TRACKERS = 3.0;

  private final Environment environment;
  private final MapService mapService;
  private final S3StorageService s3StorageService;
  private final TimeService timeService;
  private final RideTrackerDslRepository rideTrackerDslRepository;
  private final RideDslRepository rideDslRepository;

  public List<RideTracker> getTrackersForRide(Ride ride) {
    if (ride == null) {
      return Collections.emptyList();
    }
    return rideTrackerDslRepository.findAllTrackerRecord(ride.getId());
  }

  @Transactional
  public void updateCachedRideTracker(Long rideId, RideTracker tracker, Date eventTime)
    throws BadRequestException {
    // validate input data
    validateTrackers(Collections.singletonList(tracker));
    updateRideTracker(rideId, tracker, eventTime);
  }

  public RideTracker endRide(Long rideId, RideTracker rideTracker) {
    RideTracker lastRecord = rideTrackerDslRepository.findLastRecord(rideId);
    if (lastRecord != null && lastRecord.getSequence() == Long.MAX_VALUE) {
      return lastRecord;
    }
    updateRideTracker(rideId, rideTracker);
    List<RideTracker> trackers = rideTrackerDslRepository.findAllTrackerRecord(rideId);
    return recalculateTrackers(trackers);
  }

  public byte[] saveStaticImage(Ride ride) {
    try {
      Iterable<RideTracker> trackers = rideTrackerDslRepository.findValidTrackerRecord(ride);
      List<LatLng> points = StreamSupport.stream(trackers.spliterator(), false)
        .map(t -> new LatLng(t.getLatitude(), t.getLongitude()))
        .collect(Collectors.toList());

      byte[] imageData = mapService.generateMap(points);
      ride.setRideMap(s3StorageService.uploadPrivateFile(Constants.RIDE_MAP_FOLDER, imageData));
      ride.setRideMapMinimized(s3StorageService.uploadPrivateFile(Constants.RIDE_MAP_FOLDER, mapService.generateMapMinimized(points)));
      return imageData;
    } catch (Exception e) {
      log.error("Error while saving image", e);
      return new byte[0];
    }
  }

  public RideTracker updateRideTracker(ActiveDriver activeDriver, RideTracker rideTracker) {
    Ride ride = rideDslRepository.findActiveByActiveDriver(activeDriver);
    if (ride == null) {
      return rideTracker;
    }
    return updateRideTracker(ride.getId(), rideTracker);
  }

  public RideTracker updateRideTracker(Long rideId, RideTracker rideTracker) {
    return updateRideTracker(rideId, rideTracker, timeService.getCurrentDate());
  }

  public RideTracker updateRideTracker(Long rideId, RideTracker rideTracker, Date current) {
    if (rideId == null) {
      return rideTracker;
    }
    rideTracker.setRideId(rideId);
    if (rideTracker.getSequence() == null) {
      rideTracker.setSequence(current.getTime() / 1000);
    }
    rideTracker.setTrackedOn(current);
    return rideTrackerDslRepository.saveAny(rideTracker);
  }

  private RideTracker recalculateTrackers(List<RideTracker> trackers) {
    RideTracker current = null;
    if (CollectionUtils.isNotEmpty(trackers)) {
      RideTracker previous = trackers.get(0);
      previous.setDistanceTravelled(BigDecimal.ZERO);
      int countValidTrackers = 1;
      AtomicInteger lastNotGoogledTrackerIndex = new AtomicInteger(0);
      for (int i = 1; i < trackers.size(); i++) {
        current = trackers.get(i);
        current = insertMiddlePointsIfNeeded(trackers, previous, lastNotGoogledTrackerIndex, i, current);
        if (shouldInvalidateRideTracker(trackers, current, previous, i, countValidTrackers)) {
          invalidateTracker(current);
        } else {
          countValidTrackers++;
          previous = current;
        }
      }
      rideTrackerDslRepository.saveAnyMany(trackers);
    }
    return current;
  }

  private RideTracker insertMiddlePointsIfNeeded(List<RideTracker> trackers, RideTracker previous, AtomicInteger lastNotGoogledTrackerIndex, int i, RideTracker current) {
    BigDecimal directDistance = calculateDirectDistance(previous, current);
    if (isProd() && i > lastNotGoogledTrackerIndex.get() && needToCalculateMissingPath(previous, current, directDistance)) {
      List<LatLng> middlePoints = mapService.getGooglePointsBetween(previous.getLatitude(), previous.getLongitude(), current.getLatitude(), current.getLongitude());
      if (CollectionUtils.isNotEmpty(middlePoints)) {
        trackers.addAll(i, createRideTrackersFromPoints(current, previous, middlePoints));
        current = trackers.get(i);
        lastNotGoogledTrackerIndex.set(i + middlePoints.size());
        current.setDistanceTravelled(previous.getDistanceTravelled().add(calculateDirectDistance(previous, current)));
        return current;
      }
    }
    current.setDistanceTravelled(previous.getDistanceTravelled().add(directDistance));
    return current;
  }

  private List<RideTracker> createRideTrackersFromPoints(RideTracker current, RideTracker previous, List<LatLng> middlePoints) {
    Long currentSequence = current.getSequence();
    AtomicInteger index = new AtomicInteger(1);
    long startTime = previous.getTrackedOn().getTime();
    long diffInMillisPerMiddlePoint = (current.getTrackedOn().getTime() - startTime) / (middlePoints.size() + 1);
    return middlePoints.stream()
      .map(p -> newTracker(p, new Date(startTime + diffInMillisPerMiddlePoint * index.getAndIncrement()), previous.getRideId()))
      .filter(p -> p.getSequence() < currentSequence)
      .collect(Collectors.toList());
  }

  private RideTracker newTracker(LatLng p, Date date, Long rideId) {
    return RideTracker.builder().latitude(p.lat).longitude(p.lng)
      .sequence(date.getTime() / 1000).trackedOn(date).valid(true).rideId(rideId).build();
  }

  private boolean needToCalculateMissingPath(RideTracker previous, RideTracker current, BigDecimal directDistance) {
    return enoughTimeHasPassed(previous, current) && areDistantEnough(directDistance);
  }

  private boolean areDistantEnough(BigDecimal directDistance) {
    return BigDecimal.valueOf(DISTANCE_500M).compareTo(directDistance) < 0;
  }

  private boolean enoughTimeHasPassed(RideTracker previous, RideTracker current) {
    return current.getTrackedOn().getTime() - previous.getTrackedOn().getTime() > TEN_SECONDS_IN_MILLIS;
  }

  private boolean shouldInvalidateRideTracker(List<RideTracker> trackers, RideTracker current, RideTracker previous, int i, int countValidTrackers) {
    //if we have more than two trackers - there are straight lines that
    return trackers.size() > 2 &&
      //we filter every tracker except first
      //we filter end tracker only if there were at least two valid trackers so far
      (i < trackers.size() - 1 || (i == trackers.size() - 1 && countValidTrackers > 1))
      //and has speed more than 60m/s
      && isHyperSpeed(previous, current);
  }

  private void invalidateTracker(RideTracker current) {
    current.setDistanceTravelled(null);
    current.setValid(false);
  }

  private boolean isHyperSpeed(RideTracker previous, RideTracker current) {
    double secondsBetweenTrackers;
    if (previous.getSequence() == 0) {
      //start point will have sequence 0
      secondsBetweenTrackers = (current.getTrackedOn().getTime() - previous.getTrackedOn().getTime()) / (double) 1000;
      if (Double.compare(0.0, secondsBetweenTrackers) == 0) {
        secondsBetweenTrackers = STUB_TIME_BETWEEN_TRACKERS;
      }
    } else {
      secondsBetweenTrackers = current.getSequence() - (double) previous.getSequence();
      if (Double.compare(0.0, secondsBetweenTrackers) == 0) {
        //if we have time difference 0 between two locations we are skipping it
        //there are two cases:
        //1. app sends sequence 0 (lat and lng as well) that equals to out start sequence that we set to 0
        //2. we have documented example of ride 194515 where we got two same sequences one with invalid longitude = 0
        return Boolean.TRUE;
      }
    }

    // we filter the speeds faster than 60 meters per second
    BigDecimal distanceTravelled = current.getDistanceTravelled().subtract(previous.getDistanceTravelled());
    return distanceTravelled.divide(BigDecimal.valueOf(secondsBetweenTrackers), 0, RoundingMode.DOWN).intValue() > 60;
  }

  private BigDecimal calculateDirectDistance(RideTracker p1, RideTracker p2) {
    // Use the Haversine formula
    return new BigDecimal(Math.round(MapUtils.calculateDirectDistance(p1.getLongitude(), p1.getLatitude(), p2.getLongitude(), p2.getLatitude())));
  }

  private void validateTrackers(List<RideTracker> locations) throws BadRequestException {
    if (locations == null || locations.isEmpty()) {
      throw new BadRequestException("No locations specified");
    }

    for (RideTracker location : locations) {
      if (location.getLatitude() == null) {
        throw new BadRequestException("Latitude cannot be empty");
      }
      if (location.getLongitude() == null) {
        throw new BadRequestException("Longitude cannot be empty");
      }
      if (location.getSequence() == null) {
        throw new BadRequestException("Sequence cannot be empty");
      }
    }

    long count = locations.stream().map(RideTracker::getSequence).distinct().count();
    if (count != locations.size()) {
      throw new BadRequestException("Sequence numbers are not unique");
    }
  }

  /**
   * Should be removed once test envs support directions api
   * @return
   */
  @Deprecated
  private boolean isProd() {
    return environment.acceptsProfiles("prod");
  }

}
