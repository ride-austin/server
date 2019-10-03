package com.rideaustin.service.areaqueue;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import com.rideaustin.service.config.AreaQueueConfig;

@Service
public class AreaQueuePenaltyService {

  private final AreaQueueConfig areaQueueConfig;
  private final RedisTemplate redisTemplate;
  private final String prefix;

  @Inject
  public AreaQueuePenaltyService(AreaQueueConfig areaQueueConfig, RedisTemplate redisTemplate, Environment environment) {
    this.areaQueueConfig = areaQueueConfig;
    this.redisTemplate = redisTemplate;
    this.prefix = environment.getProperty("cache.redis.key.prefix", String.class, "");
  }

  public void penalize(long driverId) {
    if (areaQueueConfig.isPenaltyEnabled()) {
      redisTemplate.execute(new SessionCallback() {
        @Override
        public Object execute(RedisOperations operations) {
          operations.opsForValue().set(getPenaltyKey(driverId), "1", areaQueueConfig.getPenaltyTimeout(), TimeUnit.SECONDS);
          return null;
        }
      });
    }
  }

  public boolean isPenalized(long driverId) {
    return areaQueueConfig.isPenaltyEnabled() && (boolean) redisTemplate.execute(new SessionCallback<Boolean>() {
      @Override
      public Boolean execute(RedisOperations operations) {
        return operations.hasKey(getPenaltyKey(driverId));
      }
    });
  }

  public String getPenaltyKey(long driverId) {
    return String.format("%s:QUEUE-PENALTY:%d", prefix, driverId);
  }
}
