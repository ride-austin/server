package com.rideaustin.application.cache;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

import com.google.common.collect.ImmutableMap;

public class CacheServiceTest {

  private CacheService testedInstance;

  @Mock
  private ApplicationContext applicationContext;

  private StubCacheItem stubCacheItem;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new CacheService(applicationContext);
    stubCacheItem = new StubCacheItem();
    when(applicationContext.getBeansOfType(any())).thenReturn(ImmutableMap.of("stubCacheItem", stubCacheItem));
  }

  @Test
  public void reloadAllCacheItems() throws Exception {
    testedInstance.reloadAllCacheItems();

    assertTrue(stubCacheItem.refreshed);
  }

  @Test
  public void reloadAllCacheItemsForced() throws Exception {
    testedInstance.reloadAllCacheItems(true);

    assertTrue(stubCacheItem.refreshed);
    assertTrue(stubCacheItem.forced);
  }

  static class StubCacheItem implements CacheItem {

    private boolean refreshed = false;
    private boolean forced = false;

    @Override
    public void refreshCache() throws RefreshCacheException {
      this.refreshed = true;
    }

    @Override
    public void refreshCache(boolean force) throws RefreshCacheException {
      this.forced = true;
      refreshCache();
    }

    @Override
    public Map getAllCacheItems() {
      return null;
    }

    @Override
    public String getCacheName() {
      return null;
    }
  }
}