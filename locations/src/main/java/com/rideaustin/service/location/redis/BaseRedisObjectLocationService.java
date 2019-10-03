package com.rideaustin.service.location.redis;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import com.google.maps.model.LatLng;
import com.rideaustin.model.Area;
import com.rideaustin.model.LocationAware;
import com.rideaustin.service.location.ObjectLocationService;
import com.rideaustin.service.location.enums.LocationType;
import com.rideaustin.utils.GeometryUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public abstract class BaseRedisObjectLocationService<T extends LocationAware> implements ObjectLocationService<T> {

  protected final RedisTemplate<byte[], byte[]> redisTemplate;
  protected final String keyPrefix;

  @Inject
  public BaseRedisObjectLocationService(RedisTemplate redisTemplate, Environment environment) {
    this.redisTemplate = redisTemplate;
    keyPrefix = environment.getProperty("cache.redis.key.prefix", String.class, "");
  }

  @Override
  public T getById(Long ownerId, LocationType type) {
    return redisTemplate.execute(new SessionCallback<T>() {
      @Override
      public T execute(RedisOperations operations) {
        return (T) operations.opsForValue().get(generateKey(ownerId, type));
      }
    });
  }

  @Override
  public List<T> getByIds(Collection<Long> ownerIds, LocationType type) {
    return redisTemplate.execute(new SessionCallback<List<T>>() {
      @Override
      public List<T> execute(RedisOperations operations) {
        final List<String> keys = ownerIds.stream().map(id -> generateKey(id, type)).collect(Collectors.toList());
        return operations.opsForValue().multiGet(keys);
      }
    });
  }

  protected String generateKey(Long ownerId, LocationType type) {
    return generateKey(String.valueOf(ownerId), type);
  }

  protected String generateKey(String ownerId, LocationType type) {
    return String.format("%s:%s:%s-%s", keyPrefix, getOwner(), type, ownerId);
  }

  protected String generateWildCard() {
    return generateKey("*", LocationType.ACTIVE_DRIVER);
  }

  protected abstract String getOwner();

  protected boolean within(T location, Area area) {
    return location != null && GeometryUtils.isInsideArea(area, new LatLng(location.getLatitude(), location.getLongitude()));
  }

}
