package com.rideaustin.service.surgepricing;

import static com.rideaustin.service.surgepricing.SurgePricingService.ACCEPTED_RIDE_STATUSES;
import static com.rideaustin.service.surgepricing.SurgePricingService.REQUESTED_RIDE_STATUSES;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.rideaustin.model.GeolocationLog;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.redis.RedisSurgeArea;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.dsl.SurgeAreaDslRepository;
import com.rideaustin.repo.redis.SurgeAreaRedisRepository;
import com.rideaustin.service.ActiveDriverLocationService;
import com.rideaustin.service.GeolocationLogService;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.service.user.CarTypesUtils;
import com.rideaustin.utils.GeometryUtils;
import com.sromku.polygon.Point;
import com.sromku.polygon.Polygon;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SurgePricingStatsAggregator {

  private static final Set<RideStatus> ALL_RIDE_STATUSES = Sets.union(REQUESTED_RIDE_STATUSES, ACCEPTED_RIDE_STATUSES);

  private final SurgeAreaDslRepository surgeAreaDslRepository;
  private final SurgeAreaRedisRepository surgeAreaRedisRepository;
  private final SurgePricingService surgePricingService;
  private final GeolocationLogService geolocationLogService;
  private final ActiveDriverLocationService activeDriverLocationService;

  /**
   * Gather following statistics for each surge area in given city:
   * <ul>
   * <li>Number of requested rides within the area</li>
   * <li>Number of accepted ride requests within the area</li>
   * <li>Number of "eyeballs" - event of opening Rider App while not in a ride within the area</li>
   * <li>Number of available cars within the area</li>
   * <li>Number of available, requested and occupied cars within the area</li>
   * </ul>
   *
   * @param currentDate
   * @param startDate
   * @param cityId
   * @return
   */
  List<RedisSurgeArea> updateSurgeAreasStats(Date currentDate, Date startDate, Long cityId) {
    //aggregate active drivers
    List<OnlineDriverDto> activeDrivers = new ArrayList<>(activeDriverLocationService.getActiveDriversByStatus(ActiveDriverStatus.AVAILABLE));
    activeDrivers.addAll(activeDriverLocationService.getActiveDriversByStatus(ActiveDriverStatus.RIDING));
    activeDrivers.removeIf(ad -> !cityId.equals(ad.getCityId()));
    Map<Long, Map<String, Integer>> allActiveDriversPerArea = aggregateActiveDriversPerArea(activeDrivers, cityId);
    Map<Long, Map<String, Integer>> availableActiveDriversPerArea = aggregateAvailableActiveDriversPerArea(activeDrivers, cityId);

    //aggregate rides
    List<Ride> surgeAreaRides = surgeAreaDslRepository.findSurgeAreaRides(cityId, startDate, ALL_RIDE_STATUSES);
    Map<Long, Map<String, Integer>> requestedRidesPerArea = aggregateRidesPerArea(surgeAreaRides, r -> REQUESTED_RIDE_STATUSES.contains(r.getStatus()));
    Map<Long, Map<String, Integer>> acceptedRidesPerArea = aggregateRidesPerArea(surgeAreaRides, r -> ACCEPTED_RIDE_STATUSES.contains(r.getStatus()));

    //aggregate geolog
    Date fiveMinutesAgo = Date.from(Instant.ofEpochMilli(currentDate.getTime()).minusSeconds(300));
    List<GeolocationLog> geolog5minutes = geolocationLogService.findBetweenDates(fiveMinutesAgo, currentDate);

    List<RedisSurgeArea> existingSurgeAreas = surgeAreaRedisRepository.findByCityId(cityId);

    Map<Long, Map<String, Integer>> eyeBallsPerArea = aggregateEyeBallsPerArea(existingSurgeAreas, geolog5minutes, cityId);

    clearSurgeAreaValues(existingSurgeAreas);

    populateRequestedRides(existingSurgeAreas, requestedRidesPerArea);
    populateAcceptedRides(existingSurgeAreas, acceptedRidesPerArea);
    populateEyeBalls(existingSurgeAreas, eyeBallsPerArea);
    populateNumberOfCars(existingSurgeAreas, allActiveDriversPerArea);
    populateNumberOfAvailableCars(existingSurgeAreas, availableActiveDriversPerArea);
    return existingSurgeAreas;
  }

  private Map<Long, Map<String, Integer>> aggregateActiveDriversPerArea(List<OnlineDriverDto> activeDrivers, Long cityId) {
    return aggregateActiveDriversPerAreaUsingPredicate(activeDrivers, cityId, ad -> true);
  }

  private Map<Long, Map<String, Integer>> aggregateAvailableActiveDriversPerArea(List<OnlineDriverDto> activeDrivers, Long cityId) {
    return aggregateActiveDriversPerAreaUsingPredicate(activeDrivers, cityId, ad -> ad.getStatus() == ActiveDriverStatus.AVAILABLE);
  }

  private Map<Long, Map<String, Integer>> aggregateEyeBallsPerArea(List<RedisSurgeArea> existingSurgeAreas, List<GeolocationLog> geolog5minutes, Long cityId) {
    List<RedisSurgeArea> cityAreas = surgeAreaRedisRepository.findByCityId(cityId);
    List<Pair<GeolocationLog, List<RedisSurgeArea>>> last5MinutesAreas = geolog5minutes
      .stream()
      .map(eb -> Pair.of(eb, surgePricingService.findByCoordinates(eb.getLocationLat(), eb.getLocationLng(), cityAreas)))
      .collect(toList());
    Map<Long, Map<String, Integer>> result = existingSurgeAreas.stream().collect(Collectors.toMap(RedisSurgeArea::getId, RedisSurgeArea::getCarCategoriesNumberOfEyeballs));
    Map<Long, Map<String, Integer>> last5MinutesCounts = calculateEyeballsPerArea(last5MinutesAreas);
    for (Map.Entry<Long, Map<String, Integer>> entry : result.entrySet()) {

      Map<String, Integer> currentEyeballsPerCarCategory = entry.getValue();
      for (Map.Entry<String, Integer> categoryEntry : currentEyeballsPerCarCategory.entrySet()) {
        int updateEyeballs = BigDecimal.valueOf(categoryEntry.getValue())
          .multiply(BigDecimal.valueOf(5).divide(BigDecimal.valueOf(6), 10, RoundingMode.FLOOR)).intValue();

        if (last5MinutesCounts.containsKey(entry.getKey())) {
          Map<String, Integer> newEyeballs = last5MinutesCounts.get(entry.getKey());
          if (newEyeballs.containsKey(categoryEntry.getKey())) {
            updateEyeballs = updateEyeballs + newEyeballs.get(categoryEntry.getKey());
          }
        }
        currentEyeballsPerCarCategory.put(categoryEntry.getKey(), updateEyeballs);
      }
      entry.setValue(currentEyeballsPerCarCategory);
    }
    return result;
  }

  private Map<Long, Map<String, Integer>> calculateEyeballsPerArea(List<Pair<GeolocationLog, List<RedisSurgeArea>>> currentAreas) {
    Map<Long, Map<String, Integer>> result = Maps.newHashMap();
    for (Pair<GeolocationLog, List<RedisSurgeArea>> areas : currentAreas) {
      for (RedisSurgeArea surgeArea : areas.getRight()) {
        if (!result.containsKey(surgeArea.getId())) {
          result.put(surgeArea.getId(), new HashMap<>());
        }
        if (!result.get(surgeArea.getId()).containsKey(areas.getLeft().getCarType().getCarCategory())) {
          result.get(surgeArea.getId()).put(areas.getLeft().getCarType().getCarCategory(), 0);
        }
        result.get(surgeArea.getId()).put(areas.getLeft().getCarType().getCarCategory(),
          result.get(surgeArea.getId()).get(areas.getLeft().getCarType().getCarCategory()) + 1);
      }
    }
    return result;
  }

  private Map<Long, Map<String, Integer>> aggregateRidesPerArea(List<Ride> surgeAreaRides, Predicate<Ride> predicate) {
    return surgeAreaRides.stream()
      .filter(predicate)
      .collect(groupingBy(Ride::getStartAreaId, groupingBy(r -> r.getRequestedCarType().getCarCategory(), reducing(0, e -> 1, Integer::sum))));
  }

  private Map<Long, Map<String, Integer>> aggregateActiveDriversPerAreaUsingPredicate(List<OnlineDriverDto> activeDrivers, Long cityId, Predicate<OnlineDriverDto> predicate) {
    List<OnlineDriverDto> filteredDrivers = activeDrivers.stream().filter(predicate).collect(Collectors.toList());
    Map<String, Set<Point>> pointsPerCategory = new HashMap<>();
    for (OnlineDriverDto driver : filteredDrivers) {
      Set<String> categories = CarTypesUtils.fromBitMask(driver.getAvailableCarCategoriesBitmask());
      for (String category : categories) {
        Point point = new Point(driver.getLatitude(), driver.getLongitude());
        if (pointsPerCategory.containsKey(category)) {
          pointsPerCategory.get(category).add(point);
        } else {
          pointsPerCategory.put(category, Sets.newHashSet(point));
        }
      }
    }
    Map<Long, Polygon> polygons = surgeAreaRedisRepository.findByCityId(cityId)
      .stream()
      .collect(Collectors.toMap(
        RedisSurgeArea::getId,
        sa -> GeometryUtils.buildPolygon(sa.getAreaGeometry().getCsvGeometry())
      ));
    return polygons.entrySet().stream().collect(Collectors.toMap(
      Map.Entry::getKey,
      e -> countActiveDrivers(e.getValue(), pointsPerCategory)
    ));
  }

  private Map<String, Integer> countActiveDrivers(Polygon value, Map<String, Set<Point>> pointsPerCategory) {
    return pointsPerCategory.entrySet().stream().collect(Collectors.toMap(
      Map.Entry::getKey,
      e -> (int) e.getValue().stream().filter(value::contains).count()
    ));
  }

  private void clearSurgeAreaValues(List<RedisSurgeArea> surgeAreas) {
    surgeAreas.forEach(sa -> {
      Map<String, Integer> numbers = CarTypesUtils.fromBitMask(sa.getCarCategoriesBitmask()).stream().collect(Collectors.toMap(e -> e, e -> 0));
      sa.setNumberOfAcceptedRides(new HashMap<>(numbers));
      sa.setNumberOfRequestedRides(new HashMap<>(numbers));
      sa.setCarCategoriesNumberOfEyeballs(new HashMap<>(numbers));
      sa.setNumberOfAvailableCars(new HashMap<>(numbers));
      sa.setNumberOfCars(new HashMap<>(numbers));
      sa.getRecommendedSurgeMapping().clear();
    });
  }

  private void populateEyeBalls(List<RedisSurgeArea> surgeAreas, Map<Long, Map<String, Integer>> eyeBalls) {
    surgeAreas.stream()
      .filter(sa -> eyeBalls.containsKey(sa.getId()))
      .forEach(sa -> {
        for (Map.Entry<String, Integer> entry : eyeBalls.get(sa.getId()).entrySet()) {
          sa.getCarCategoriesNumberOfEyeballs().replace(entry.getKey(), entry.getValue());
        }
      });
  }

  private void populateRequestedRides(List<RedisSurgeArea> surgeAreas, Map<Long, Map<String, Integer>> requestedRidesPerArea) {
    surgeAreas.stream()
      .filter(sa -> requestedRidesPerArea.containsKey(sa.getAreaGeometry().getId()))
      .forEach(sa -> {
        for (Map.Entry<String, Integer> entry : requestedRidesPerArea.get(sa.getAreaGeometry().getId()).entrySet()) {
          sa.getNumberOfRequestedRides().replace(entry.getKey(), entry.getValue());
        }
      });
  }

  private void populateAcceptedRides(List<RedisSurgeArea> surgeAreas, Map<Long, Map<String, Integer>> acceptedRidesPerArea) {
    surgeAreas
      .stream()
      .filter(sa -> acceptedRidesPerArea.containsKey(sa.getAreaGeometry().getId()))
      .forEach(sa -> {
        for (Map.Entry<String, Integer> entry : acceptedRidesPerArea.get(sa.getAreaGeometry().getId()).entrySet()) {
          sa.getNumberOfAcceptedRides().replace(entry.getKey(), entry.getValue());
        }
      });
  }

  private void populateNumberOfCars(List<RedisSurgeArea> surgeAreas, Map<Long, Map<String, Integer>> allActiveDriversPerArea) {
    surgeAreas.stream()
      .filter(sa -> allActiveDriversPerArea.containsKey(sa.getId()))
      .forEach(sa -> {
        for (Map.Entry<String, Integer> entry : allActiveDriversPerArea.get(sa.getId()).entrySet()) {
          sa.getNumberOfCars().replace(entry.getKey(), entry.getValue());
        }
      });
  }

  private void populateNumberOfAvailableCars(List<RedisSurgeArea> surgeAreas, Map<Long, Map<String, Integer>> availableActiveDriversPerArea) {
    surgeAreas
      .stream()
      .filter(sa -> availableActiveDriversPerArea.containsKey(sa.getId()))
      .forEach(sa -> {
        for (Map.Entry<String, Integer> entry : availableActiveDriversPerArea.get(sa.getId()).entrySet()) {
          sa.getNumberOfAvailableCars().replace(entry.getKey(), entry.getValue());
        }
      });
  }
}
