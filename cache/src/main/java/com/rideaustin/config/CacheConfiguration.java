package com.rideaustin.config;

import java.util.Arrays;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.inject.Named;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;

import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.maps.model.LatLng;
import com.rideaustin.model.BaseEntity;
import com.rideaustin.model.LocationAware;

@Configuration
@EnableCaching
public class CacheConfiguration {

  public static final String RIDER_LOCATION_CACHE = "riderLocationCache";
  public static final String KEY_GENERATOR = "locationObjectKeyGenerator";
  public static final String DOCUMENTS_CACHE = "documentsCache";
  public static final String DOCUMENT_CACHE_KEY_GENERATOR = "documentCacheKeyGenerator";
  public static final String ETC_CACHE = "etcCache";
  public static final String CAMPAIGNS_CACHE = "campaignsCache";
  public static final String CAMPAIGN_PROVIDERS_CACHE = "campaignProvidersCache";
  public static final String ESTIMATION_CACHE = "estimationCache";

  @Bean
  @Primary
  public CacheManager cacheManager() {
    return new ConcurrentMapCacheManager() {
      @Override
      protected Cache createConcurrentMapCache(String name) {
        switch (name) {
          case RIDER_LOCATION_CACHE:
            return createExpiringCache(name, 3, TimeUnit.SECONDS, false);
          case DOCUMENTS_CACHE:
          case CAMPAIGNS_CACHE:
          case CAMPAIGN_PROVIDERS_CACHE:
            return createExpiringCache(name, 1, TimeUnit.HOURS, true);
          case ESTIMATION_CACHE:
            return createExpiringCache(name, 1, TimeUnit.MINUTES, true);
          default:
            return super.createConcurrentMapCache(name);
        }
      }
    };
  }

  @Bean
  public CacheManager etcCacheManager(Environment environment, @Named("etcCacheRedisTemplate") RedisTemplate redisTemplate) {
    RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate);
    cacheManager.setCachePrefix(cacheName -> String.format("%s:ride:et:", environment.getProperty("cache.redis.key.prefix", String.class, "")).getBytes());
    cacheManager.setExpires(ImmutableMap.of(ETC_CACHE, 60L));
    cacheManager.setUsePrefix(true);
    return cacheManager;
  }

  @Bean
  public RedisTemplate<byte[], byte[]> etcCacheRedisTemplate(JedisConnectionFactory connectionFactory) {
    RedisTemplate<byte[], byte[]> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    template.setKeySerializer(new GenericToStringSerializer<>(Long.class));
    template.setValueSerializer(new GenericToStringSerializer<>(Long.class));
    return template;
  }

  private Cache createExpiringCache(String name, int expiration, TimeUnit expirationUnit, boolean allowNullValues) {
    if (allowNullValues) {
      return new ConcurrentMapCache(name,
        CacheBuilder
          .newBuilder()
          .expireAfterWrite(expiration, expirationUnit)
          .build()
          .asMap(),
        true);
    } else {
      return new NullSkipConcurrentMapCache(name,
        CacheBuilder
          .newBuilder()
          .expireAfterWrite(expiration, expirationUnit)
          .build()
          .asMap()
      );
    }
  }

  @Bean
  public KeyGenerator estimationKeyGenerator() {
    return (target, method, params) -> {
      if (params.length == 2 && params[0] instanceof LatLng && params[1] instanceof LatLng) {
        return Arrays.stream(params).map(Object::toString).collect(Collectors.joining(":"));
      }
      return SimpleKey.EMPTY;
    };
  }

  @Bean
  public KeyGenerator locationObjectKeyGenerator() {
    return (target, method, params) -> {
      if (params[0] instanceof Long) {
        return params[0];
      } else if (params[0] instanceof LocationAware) {
        return ((LocationAware) params[0]).getId();
      } else if (params[1] instanceof LocationAware) {
        return ((LocationAware) params[1]).getId();
      } else if (params[1] instanceof BaseEntity) {
        return ((BaseEntity) params[1]).getId();
      }
      return SimpleKey.EMPTY;
    };
  }

  public static class NullSkipConcurrentMapCache extends ConcurrentMapCache {

    NullSkipConcurrentMapCache(String name, ConcurrentMap<Object, Object> store) {
      super(name, store, false);
    }

    @Override
    public void put(Object key, Object value) {
      if (value != null) {
        super.put(key, value);
      }
    }

  }
}
