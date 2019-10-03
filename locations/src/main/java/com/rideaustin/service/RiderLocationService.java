package com.rideaustin.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.config.CacheConfiguration;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.Area;
import com.rideaustin.model.ConfigurationItem;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.service.event.EventsNotificationService;
import com.rideaustin.service.location.enums.LocationType;
import com.rideaustin.service.location.model.RiderLocation;
import com.rideaustin.service.location.redis.BaseRedisObjectLocationService;

@Service
public class RiderLocationService extends BaseRedisObjectLocationService<RiderLocation> {

  private static final String RIDER_LIVE_LOCATION_CONFIG = "riderLiveLocation";
  private static final String RIDER_LIVE_LOCATION_ENABLED_KEY = "enabled";

  private final EventsNotificationService eventsNotificationService;
  private final ConfigurationItemCache configCache;
  private final int updateThreshold;

  @Inject
  public RiderLocationService(RedisTemplate redisTemplate, Environment environment, EventsNotificationService eventsNotificationService,
    ConfigurationItemCache configCache) {
    super(redisTemplate, environment);
    this.eventsNotificationService = eventsNotificationService;
    this.configCache = configCache;
    this.updateThreshold = environment.getProperty("rider.location.update.threshold", Integer.class, 5000);
  }

  public void processLocationUpdate(long riderId, long driverId, Double latitude, Double longitude) {
    Optional<ConfigurationItem> liveRiderLocationConfig = configCache.getConfigurationForClient(ClientType.RIDER)
      .stream()
      .filter(ci -> RIDER_LIVE_LOCATION_CONFIG.equals(ci.getConfigurationKey()))
      .findFirst();
    boolean enableLiveRiderLocation = false;
    if (liveRiderLocationConfig.isPresent()) {
      enableLiveRiderLocation = (boolean) ((Map) liveRiderLocationConfig.get().getConfigurationObject()).get(RIDER_LIVE_LOCATION_ENABLED_KEY);
    }
    if (latitude == null || longitude == null || !enableLiveRiderLocation) {
      return;
    }
    boolean needToUpdate = true;
    RiderLocation locationObject = this.getById(riderId, LocationType.RIDER);
    Date currentDateTime = new Date();
    if (locationObject == null) {
      locationObject = new RiderLocation(riderId, latitude, longitude);
    } else {
      needToUpdate = currentDateTime.getTime() - locationObject.getLocationUpdateDate().getTime() > updateThreshold;
    }
    if (needToUpdate) {
      locationObject.setLocationUpdateDate(currentDateTime);
      eventsNotificationService.sendRiderLocationUpdate(driverId, latitude, longitude, currentDateTime.getTime());
      this.saveOrUpdateLocationObject(locationObject);
    }
  }

  @CacheEvict(value = CacheConfiguration.RIDER_LOCATION_CACHE, keyGenerator = CacheConfiguration.KEY_GENERATOR)
  public void evictRiderLocation(long riderId) {
    this.removeLocationObject(riderId, LocationType.RIDER);
  }

  @Override
  @Cacheable(value = CacheConfiguration.RIDER_LOCATION_CACHE, keyGenerator = CacheConfiguration.KEY_GENERATOR)
  public RiderLocation getById(Long ownerId, LocationType type) {
    return super.getById(ownerId, type);
  }

  @Override
  protected String getOwner() {
    return LocationType.RIDER.name();
  }

  @Override
  public List<RiderLocation> searchActiveInsideArea(Area area, Set<ActiveDriverStatus> statuses) {
    throw new UnsupportedOperationException();
  }

  @Override
  @CachePut(value = CacheConfiguration.RIDER_LOCATION_CACHE, keyGenerator = CacheConfiguration.KEY_GENERATOR)
  public RiderLocation saveOrUpdateLocationObject(final RiderLocation object) {
    redisTemplate.execute(new SessionCallback<Object>() {
      @Override
      public Object execute(RedisOperations operations) {
        operations.multi();
        operations.opsForValue().set(generateKey(object.getId(), LocationType.RIDER), object);
        operations.exec();
        return null;
      }
    });
    return object;
  }

  @Override
  public void removeLocationObject(Long ownerId, LocationType type) {
    redisTemplate.execute(new SessionCallback<Object>() {
      @Override
      public Object execute(RedisOperations operations) {
        operations.delete(generateKey(ownerId, type));
        return null;
      }
    });
  }

  @Override
  public List<RiderLocation> getAll() {
    throw new UnsupportedOperationException();
  }

}
