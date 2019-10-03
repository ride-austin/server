package com.rideaustin.service.recovery;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.repo.dsl.ActiveDriverDslRepository;
import com.rideaustin.repo.dsl.ActiveDriversRecoveryRepository;
import com.rideaustin.service.ActiveDriverLocationService;
import com.rideaustin.service.model.OnlineDriverDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ActiveDriversRecoveryService {

  private final ActiveDriversRecoveryRepository recoveryRepository;
  private final RedisTemplate redisTemplate;
  private final ActiveDriverDslRepository activeDriverDslRepository;
  private final Environment environment;
  private final ActiveDriverLocationService activeDriverLocationService;

  @Deprecated
  public void cleanUpRidingDrivers() {
    List<OnlineDriverDto> list = (List<OnlineDriverDto>) redisTemplate.execute(new SessionCallback<List<OnlineDriverDto>>() {
      @Override
      public List<OnlineDriverDto> execute(RedisOperations operations) throws DataAccessException {
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> content = operations.opsForGeo().geoRadius(String.format("%s:GEO_INDEX", environment.getProperty("cache.redis.key.prefix", String.class, "")), new Circle(new Point(-97.743061, 30.267153), new Distance(100000, RedisGeoCommands.DistanceUnit.KILOMETERS))).getContent();
        List<String> ids = content.stream().map(GeoResult::getContent).map(RedisGeoCommands.GeoLocation::getName).collect(Collectors.toList());
        return operations.opsForValue().multiGet(ids);
      }
    });
    Map<Long, ActiveDriverStatus> redisDrivers = list.stream().collect(Collectors.toMap(OnlineDriverDto::getId, OnlineDriverDto::getStatus));
    Map<Long, ActiveDriverStatus> dbDrivers = activeDriverDslRepository.findByIds(list.stream().map(OnlineDriverDto::getId).collect(Collectors.toSet()))
      .stream().collect(Collectors.toMap(ActiveDriver::getId, ActiveDriver::getStatus));
    MapDifference<Long, ActiveDriverStatus> difference = Maps.difference(redisDrivers, dbDrivers);
    if (!difference.areEqual()) {
      for (Long id : difference.entriesDiffering().keySet()) {
        if (dbDrivers.get(id) == ActiveDriverStatus.INACTIVE) {
          activeDriverLocationService.removeLocationObject(id);
        }
      }
    }
    List<Long> candidates = recoveryRepository.findRidingStuckCandidates();
    if (!candidates.isEmpty()) {
      int updated = recoveryRepository.cleanUpRidingDrivers(candidates);
      log.info(updated + " RIDING active drivers were deactivated");
    }
  }

  public void setAsAvailable(long id) {
    activeDriverDslRepository.setRidingDriverAsAvailable(id);
  }

}
