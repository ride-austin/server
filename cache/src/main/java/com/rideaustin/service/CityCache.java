package com.rideaustin.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.rideaustin.application.cache.CacheItem;
import com.rideaustin.model.City;
import com.rideaustin.repo.dsl.CityDslRepository;
import com.rideaustin.utils.GeometryUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CityCache implements CacheItem {

  private static final String CITIES_CACHE = "citiesCache";
  private List<City> cachedCities;
  private Map<Long, City> cachedCitiesMap = Maps.newHashMap();
  private Map<Long, Long> cachedCitiesBitMasks = Maps.newHashMap();
  private final CityDslRepository cityDslRepository;

  public List<City> getAllCities() {
    return cachedCities;
  }

  public City getCity(long cityId) {
    if (cachedCitiesMap == null) {
      return null;
    }
    return cachedCitiesMap.get(cityId);
  }

  @PostConstruct
  @Override
  public void refreshCache() {
    Map newCachedCitiesMap = Maps.newHashMap();
    Map newCachedCitiesBitMasks = Maps.newHashMap();
    List<City> newCachedCities = cityDslRepository.findAllEnabled();
    newCachedCities.forEach(c -> {
      c.getAreaGeometry().setPolygon(GeometryUtils.buildPolygon(c.getAreaGeometry().getCsvGeometry()));
      newCachedCitiesMap.put(c.getId(), c);
      newCachedCitiesBitMasks.put(c.getBitmask(), c.getId());
    });

    setCacheState(newCachedCitiesMap, newCachedCities, newCachedCitiesBitMasks);
  }

  private synchronized void setCacheState(Map cachedCitiesMap, List cachedCities, Map cachedCitiesBitMasks) {
    this.cachedCities = cachedCities;
    this.cachedCitiesMap = cachedCitiesMap;
    this.cachedCitiesBitMasks = cachedCitiesBitMasks;
  }

  @Override
  public Map getAllCacheItems() {
    return ImmutableMap.of("cities", cachedCities);
  }

  @Override
  public String getCacheName() {
    return CITIES_CACHE;
  }

  public int toBitMask(Collection<Long> cityIds) {
    return cityIds.stream()
      .mapToInt(id -> cachedCitiesMap.get(id).getBitmask()).sum();
  }

  public Set<Long> fromBitMask(final int bitmask) {
    int bitmaskCopy = bitmask;
    Set<Long> ret = Sets.newHashSet();
    while (bitmaskCopy != 0) {
      int curr = Integer.lowestOneBit(bitmaskCopy);
      Long id = cachedCitiesBitMasks.get(curr);
      if (id != null) {
        ret.add(id);
      }
      bitmaskCopy ^= curr;
    }
    return ret;
  }
}