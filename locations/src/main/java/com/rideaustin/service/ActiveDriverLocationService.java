package com.rideaustin.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.core.env.Environment;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import com.rideaustin.model.Area;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.service.config.ActiveDriverServiceConfig;
import com.rideaustin.service.location.ObjectLocationUtil;
import com.rideaustin.service.location.enums.LocationType;
import com.rideaustin.service.location.model.LocationObject;
import com.rideaustin.service.location.redis.BaseRedisObjectLocationService;
import com.rideaustin.service.model.ActiveDriverInfo;
import com.rideaustin.service.model.ConsecutiveDeclinedRequestsData;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.service.user.CarTypesCache;
import com.rideaustin.service.user.CarTypesUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ActiveDriverLocationService extends BaseRedisObjectLocationService<OnlineDriverDto> {

  private final CarTypesCache carTypesCache;
  private final ActiveDriverServiceConfig config;

  public ActiveDriverLocationService(RedisTemplate redisTemplate, Environment environment, CarTypesCache carTypesCache,
    ActiveDriverServiceConfig config) {
    super(redisTemplate, environment);
    this.carTypesCache = carTypesCache;
    this.config = config;
  }

  public OnlineDriverDto updateActiveDriverLocation(ActiveDriverUpdateParams params, ActiveDriverInfo activeDriver, boolean eligibleForStacking) {
    OnlineDriverDto onlineDriverDto = getById(activeDriver.getId(), LocationType.ACTIVE_DRIVER);
    if (onlineDriverDto == null) {
      onlineDriverDto = new OnlineDriverDto(activeDriver);
    } else {
      if (onlineDriverDto.getAvailableCarCategoriesBitmask() != activeDriver.getAvailableCarCategoriesBitmask()) {
        onlineDriverDto.setAvailableCarCategoriesBitmask(activeDriver.getAvailableCarCategoriesBitmask());
        ConsecutiveDeclinedRequestsData currentCDRData = onlineDriverDto.getConsecutiveDeclinedRequests();
        Map<String, Integer> cleanedCDRData = cleanConsecutiveDecliningRequest(currentCDRData);
        Map<String, Integer> updatedCDRData = updateConsecutiveDecliningRequest(params.getCarCategories(), cleanedCDRData);
        onlineDriverDto.setConsecutiveDeclinedRequests(new ConsecutiveDeclinedRequestsData(updatedCDRData));
      }
      if (onlineDriverDto.getConsecutiveDeclinedRequests() == null) {
        onlineDriverDto.setConsecutiveDeclinedRequests(new ConsecutiveDeclinedRequestsData(CarTypesUtils.fromBitMask(onlineDriverDto.getAvailableCarCategoriesBitmask())));
      }
      onlineDriverDto.setAvailableDriverTypesBitmask(activeDriver.getAvailableDriverTypesBitmask());
    }
    LocationObject lo = new LocationObject(params.getLatitude(), params.getLongitude(), params.getHeading(),
      params.getSpeed(), params.getCourse());
    onlineDriverDto.setLocationObject(lo);
    onlineDriverDto.setEligibleForStacking(eligibleForStacking);
    if (activeDriver.getStatus() != null && !activeDriver.getStatus().equals(onlineDriverDto.getStatus())) {
      onlineDriverDto.setStatus(activeDriver.getStatus());
    }

    if (activeDriver.getStatus() != ActiveDriverStatus.INACTIVE) {
      return this.saveOrUpdateLocationObject(onlineDriverDto);
    }
    return onlineDriverDto;
  }

  public void removeLocationObject(Long activeDriverId) {
    this.removeLocationObject(activeDriverId, LocationType.ACTIVE_DRIVER);
  }

  public List<OnlineDriverDto> getActiveDriversByStatus(final ActiveDriverStatus status) {
    return Optional.ofNullable(redisTemplate.execute(new SessionCallback<List<OnlineDriverDto>>() {
      @Override
      public List<OnlineDriverDto> execute(RedisOperations operations) {
        Set keys = operations.opsForSet().members(generateStatusIndexKey(status));
        return operations.opsForValue().multiGet(keys);
      }
    })).orElse(new ArrayList<>());
  }

  @Override
  public OnlineDriverDto saveOrUpdateLocationObject(final OnlineDriverDto object) {
    if (object != null && object.getStatus() == ActiveDriverStatus.INACTIVE) {
      return object;
    }
    redisTemplate.execute(new SaveOrUpdateCallback(object));
    return object;
  }

  @Override
  public void removeLocationObject(Long ownerId, LocationType type) {
    redisTemplate.execute(new SessionCallback() {
      @Override
      public Object execute(RedisOperations operations) {
        operations.multi();
        String id = generateKey(ownerId, LocationType.ACTIVE_DRIVER);
        operations.delete(id);
        for (ActiveDriverStatus status : ActiveDriverStatus.values()) {
          operations.opsForSet().remove(generateStatusIndexKey(status), id);
        }
        for (String carCategory : carTypesCache.getActiveCarTypes().keySet()) {
          operations.opsForSet().remove(generateCategoryIndexKey(carCategory), id);
        }
        operations.opsForGeo().geoRemove(generateGeoIndexName(), id);
        operations.opsForSet().remove(generateStackableIndexName(), id);
        operations.opsForSet().remove(generateStackedIndexName(), id);
        operations.exec();
        return null;
      }
    });
  }

  @Override
  public List<OnlineDriverDto> getAll() {
    return redisTemplate.execute(new SessionCallback<List<OnlineDriverDto>>() {
      @Override
      public List<OnlineDriverDto> execute(RedisOperations operations) {
        return operations.opsForValue().multiGet(operations.keys(generateWildCard()));
      }
    });
  }

  public List<OnlineDriverDto> locationAround(Double latitude, Double longitude, int searchRadius, String carCategory, Integer limitByDistance) {
    return redisTemplate.execute(new SearchCallback(latitude, longitude, searchRadius, carCategory, limitByDistance));
  }

  @Override
  public List<OnlineDriverDto> searchActiveInsideArea(Area area, Set<ActiveDriverStatus> statuses) {
    Predicate<OnlineDriverDto> predicate = d -> within(d, area);
    List<OnlineDriverDto> drivers = new ArrayList<>();
    for (ActiveDriverStatus status : statuses) {
      drivers.addAll(getActiveDriversByStatus(status));
    }
    return drivers.stream().filter(predicate).collect(Collectors.toList());
  }

  public OnlineDriverDto updateActiveDriverLocationStatus(Long id, ActiveDriverStatus status) {
    OnlineDriverDto onlineDriver = this.getById(id, LocationType.ACTIVE_DRIVER);
    if (onlineDriver != null && status != ActiveDriverStatus.INACTIVE) {
      onlineDriver.setStatus(status);
      this.saveOrUpdateLocationObject(onlineDriver);
    }
    return onlineDriver;
  }

  public void setActiveDriverAsRiding(long id, String carTypeToResetCDR) {
    OnlineDriverDto onlineDriver = this.getById(id, LocationType.ACTIVE_DRIVER);
    if (onlineDriver != null) {
      onlineDriver.setStatus(ActiveDriverStatus.RIDING);
      ConsecutiveDeclinedRequestsData declinedRequests = onlineDriver.getConsecutiveDeclinedRequests();
      declinedRequests.reset(carTypeToResetCDR);
      onlineDriver.setConsecutiveDeclinedRequests(declinedRequests);
      this.saveOrUpdateLocationObject(onlineDriver);
    }
  }

  public void updateActiveDriverStackedEligibility(long id, boolean stackEligibility) {
    OnlineDriverDto onlineDriver = getById(id, LocationType.ACTIVE_DRIVER);
    if (onlineDriver != null) {
      onlineDriver.setEligibleForStacking(stackEligibility);
      saveOrUpdateLocationObject(onlineDriver);
    }
  }

  @PostConstruct
  public void pushToUtil() {
    ObjectLocationUtil.setObjectLocationService(this);
  }

  @Override
  protected String getOwner() {
    return LocationType.ACTIVE_DRIVER.name();
  }

  private String generateGeoIndexName() {
    return String.format("%s:GEO_INDEX", keyPrefix);
  }

  private String generateCategoryIndexKey(String carCategory) {
    return String.format("%s:%s", keyPrefix, carCategory);
  }

  private String generateStatusIndexKey(ActiveDriverStatus status) {
    return String.format("%s:%s", keyPrefix, status);
  }

  private String generateStackableIndexName() {
    return String.format("%s:STACKABLE", keyPrefix);
  }

  private String generateStackedIndexName() {
    return String.format("%s:STACKED", keyPrefix);
  }

  private Map<String, Integer> updateConsecutiveDecliningRequest(Set<String> carCategories, Map<String, Integer> cleanedCDRData) {
    Map<String, Integer> updatedCDRData = new HashMap<>();
    for (String cc : carCategories) {
      updatedCDRData.put(cc, cleanedCDRData.getOrDefault(cc, 0));
    }
    return updatedCDRData;
  }

  private Map<String, Integer> cleanConsecutiveDecliningRequest(Map<String, Integer> currentCDRData) {
    Map<String, Integer> cleanedCDRData = new HashMap<>();

    currentCDRData.keySet()
      .stream()
      .filter(key -> currentCDRData.get(key) < config.getDriverMaxDeclinedRequests())
      .forEach(key -> cleanedCDRData.put(key, currentCDRData.get(key)));
    return cleanedCDRData;
  }

  @RequiredArgsConstructor
  private class SaveOrUpdateCallback implements SessionCallback {

    private final OnlineDriverDto object;

    @Override
    public Object execute(RedisOperations operations) {
      operations.multi();
      String id = generateKey(object.getId(), LocationType.ACTIVE_DRIVER);
      operations.opsForValue().set(id, object);
      for (ActiveDriverStatus status : ActiveDriverStatus.values()) {
        operations.opsForSet().remove(generateStatusIndexKey(status), id);
      }
      for (String carCategory : carTypesCache.getActiveCarTypes().keySet()) {
        operations.opsForSet().remove(generateCategoryIndexKey(carCategory), id);
      }
      for (String carCategory : carTypesCache.fromBitMask(object.getAvailableCarCategoriesBitmask())) {
        operations.opsForSet().add(generateCategoryIndexKey(carCategory), id);
      }
      operations.opsForSet().add(generateStatusIndexKey(object.getStatus()), id);
      operations.opsForGeo().geoRemove(generateGeoIndexName(), id);
      operations.opsForGeo().geoAdd(generateGeoIndexName(), new RedisGeoCommands.GeoLocation<>(id, new Point(object.getLongitude(), object.getLatitude())));
      operations.opsForSet().remove(generateStackableIndexName(), id);
      if (object.isEligibleForStacking()) {
        operations.opsForSet().add(generateStackableIndexName(), id);
      }
      operations.exec();
      return null;
    }
  }

  private class SearchCallback implements SessionCallback<List<OnlineDriverDto>> {
    private final Double latitude;
    private final Double longitude;
    private final int searchRadius;
    private final String carCategory;
    private final Integer limitByDistance;

    public SearchCallback(Double latitude, Double longitude, int searchRadius, String carCategory, Integer limitByDistance) {
      this.latitude = latitude;
      this.longitude = longitude;
      this.searchRadius = searchRadius;
      this.carCategory = carCategory;
      this.limitByDistance = limitByDistance;
    }

    @Override
    public List<OnlineDriverDto> execute(RedisOperations operations) {
      RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
        .sortAscending();
      if (limitByDistance != null) {
        args = args.limit(limitByDistance * 10L);
      }
      GeoResults<RedisGeoCommands.GeoLocation<String>> geoResults = operations.opsForGeo().geoRadius(generateGeoIndexName(),
        new Circle(new Point(longitude, latitude), new Distance(searchRadius, RedisGeoCommands.DistanceUnit.MILES)), args);
      List<GeoResult<RedisGeoCommands.GeoLocation<String>>> content = geoResults.getContent();
      List<String> ids = content.stream().map(GeoResult::getContent)
        .map(RedisGeoCommands.GeoLocation::getName)
        .collect(Collectors.toList());
      filterByCategory(ids, carCategory, operations);
      filterByAvailability(ids, operations);
      if (ids.isEmpty()) {
        return Collections.emptyList();
      }
      if (limitByDistance != null) {
        ids = ids.subList(0, Math.min(limitByDistance, ids.size()));
      }
      return operations.opsForValue().multiGet(ids);
    }

    private void filterByCategory(List<String> ids, String carCategory, RedisOperations operations) {
      if (carCategory == null) {
        return;
      }
      Set<String> members = (Set<String>) operations.opsForSet().members(generateCategoryIndexKey(carCategory));
      ids.retainAll(members);
    }

    private void filterByAvailability(List<String> ids, RedisOperations operations) {
      Set<String> result = new HashSet<>();
      Set<String> members = (Set<String>) operations.opsForSet().members(generateStatusIndexKey(ActiveDriverStatus.AVAILABLE));
      result.addAll(members);
      result.addAll(operations.opsForSet().members(generateStackableIndexName()));
      ids.retainAll(result);
      Set<String> requested = (Set<String>) operations.opsForSet().members(generateRequestedIndexName())
        .stream()
        .map(s -> generateKey(String.valueOf(s), LocationType.ACTIVE_DRIVER))
        .collect(Collectors.toSet());
      ids.removeAll(requested);
    }

    private String generateRequestedIndexName() {
      return String.format("%s:REQUESTED", keyPrefix);
    }
  }

}
