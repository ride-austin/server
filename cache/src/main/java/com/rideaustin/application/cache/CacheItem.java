package com.rideaustin.application.cache;

import java.util.Map;

public interface CacheItem {

  void refreshCache() throws RefreshCacheException;
  default void refreshCache(boolean force) throws RefreshCacheException {
    refreshCache();
  }
  Map getAllCacheItems();
  String getCacheName();
}
