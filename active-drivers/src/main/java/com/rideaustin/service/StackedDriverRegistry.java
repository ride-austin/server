package com.rideaustin.service;

import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;

import com.rideaustin.service.location.enums.LocationType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StackedDriverRegistry {

  private static final String STACKED_KEY_TEMPLATE = "%s:STACKED";
  private static final String STACKABLE_KEY_TEMPLATE = "%s:STACKABLE";

  private final RedisTemplate redisTemplate;
  private final String prefix;

  public StackedDriverRegistry(RedisTemplate redisTemplate, Environment environment) {
    this.redisTemplate = redisTemplate;
    this.prefix = environment.getProperty("cache.redis.key.prefix", String.class, "");
  }

  public void addStacked(long id) {
    log.info(String.format("[STACK][AD %d] Moved from stackable to stacked", id));
    moveBetweenSets(id, getStackableKey(), getStackedKey());
  }

  public boolean isStacked(long id) {
    return (boolean) redisTemplate.execute(new SessionCallback<Boolean>() {
      @Override
      public Boolean execute(RedisOperations operations) {
        return operations.opsForSet().isMember(getStackedKey(), generateKey(id));
      }
    });
  }

  public void makeStackable(long id) {
    log.info(String.format("[STACK][AD %d] Moved from stacked to stackable", id));
    moveBetweenSets(id, getStackedKey(), getStackableKey());
  }

  public void removeFromStack(long id) {
    log.info(String.format("[STACK][AD %d] Removed from stacked", id));
    removeFrom(id, getStackedKey());
  }

  public void removeFromStackable(long id) {
    log.info(String.format("[STACK][AD %d] Removed from stackable", id));
    removeFrom(id, getStackableKey());
  }

  private void removeFrom(final long id, final String key) {
    redisTemplate.execute(new SessionCallback() {
      @Override
      public Object execute(RedisOperations operations) {
        String storedId = generateKey(id);
        operations.multi();
        operations.opsForSet().remove(key, storedId);
        operations.exec();
        return null;
      }
    });
  }

  private void moveBetweenSets(long id, final String source, final String target) {
    redisTemplate.execute(new SessionCallback() {
      @Override
      public Object execute(RedisOperations operations) {
        String storedId = generateKey(id);
        operations.multi();
        operations.opsForSet().remove(source, storedId);
        operations.opsForSet().add(target, storedId);
        operations.exec();
        return null;
      }
    });
  }

  private String getStackableKey() {
    return String.format(STACKABLE_KEY_TEMPLATE, prefix);
  }

  private String getStackedKey() {
    return String.format(STACKED_KEY_TEMPLATE, prefix);
  }

  private String generateKey(long id) {
    return String.format("%s:%s:%s-%s", prefix, LocationType.ACTIVE_DRIVER, LocationType.ACTIVE_DRIVER, id);
  }
}
