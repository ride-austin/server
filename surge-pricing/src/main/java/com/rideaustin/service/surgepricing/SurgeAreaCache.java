package com.rideaustin.service.surgepricing;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.rideaustin.application.cache.CacheItem;
import com.rideaustin.model.redis.RedisSurgeArea;
import com.rideaustin.repo.dsl.SurgeAreaDslRepository;
import com.rideaustin.repo.redis.SurgeAreaRedisRepository;
import com.rideaustin.utils.GeometryUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SurgeAreaCache implements CacheItem {

  private static final String SURGE_AREAS_CACHE = "surgeAreasCache";

  private final SurgeAreaDslRepository surgeAreaDslRepository;
  private final SurgeAreaRedisRepository surgeAreaRedisRepository;

  @Override
  public void refreshCache() {
    refreshCache(false);
  }

  @Override
  public void refreshCache(boolean force) {
    if (force || surgeAreaRedisRepository.count() == 0) {
      surgeAreaRedisRepository.deleteAll();
      surgeAreaDslRepository.findAllActive()
        .forEach(area -> {
            GeometryUtils.updatePolygon(area.getAreaGeometry());
            surgeAreaRedisRepository.save(new RedisSurgeArea(area));
          });
    }
  }

  @Override
  public Map getAllCacheItems() {
    return StreamSupport.stream(surgeAreaRedisRepository.findAll().spliterator(), false).collect(Collectors.toMap(
      RedisSurgeArea::getId,
      Function.identity()
    ));
  }

  @Override
  public String getCacheName() {
    return SURGE_AREAS_CACHE;
  }
}
