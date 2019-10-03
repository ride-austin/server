package com.rideaustin.application.cache;

import java.util.Map;

import javax.inject.Inject;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.config.CacheConfiguration;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CacheService {

  private final ApplicationContext applicationContext;

  @Scheduled(cron = "${cache.refresh_cron}")
  public void reloadAllCacheItems() throws RefreshCacheException {
    for (CacheItem cacheItem : getCacheItemBeans().values()) {
      cacheItem.refreshCache();
    }
  }

  @CacheEvict(cacheNames = {CacheConfiguration.CAMPAIGN_PROVIDERS_CACHE,
  CacheConfiguration.CAMPAIGNS_CACHE, CacheConfiguration.DOCUMENTS_CACHE,
  CacheConfiguration.ETC_CACHE, CacheConfiguration.RIDER_LOCATION_CACHE,
  CacheConfiguration.ESTIMATION_CACHE}, allEntries = true)
  public void reloadAllCacheItems(boolean force) throws RefreshCacheException {
    for (CacheItem cacheItem : getCacheItemBeans().values()) {
      cacheItem.refreshCache(force);
    }
  }

  private Map<String, CacheItem> getCacheItemBeans() {
    return applicationContext.getBeansOfType(CacheItem.class);
  }

}
