package com.rideaustin.service.user;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.rideaustin.application.cache.CacheItem;
import com.rideaustin.model.ride.CityDriverType;
import com.rideaustin.model.ride.DriverType;
import com.rideaustin.repo.dsl.DriverTypeDslRepository;

@Component
@DependsOn(value = "carTypesCache")
public class DriverTypeCache implements CacheItem {

  private static final String DRIVER_TYPES_CACHE = "driverTypesCache";
  private final DriverTypeDslRepository driverTypeDslRepository;

  private Map<Long, List<CityDriverType>> cityDriverTypes = Maps.newHashMap();
  private Map<String, DriverType> driverTypes = Maps.newHashMap();
  private Map<Integer, DriverType> driverTypesByMask = Maps.newHashMap();

  @Inject
  public DriverTypeCache(DriverTypeDslRepository driverTypeDslRepository) {
    this.driverTypeDslRepository = driverTypeDslRepository;
  }

  public DriverType getDriverType(String driverType) {
    if (driverType == null) {
      return null;
    }
    return this.driverTypes.get(driverType);
  }

  public Map<String, DriverType> getDriverTypes() {
    return driverTypes;
  }

  public Integer toBitMask(Collection<String> typeNames) {
    if (typeNames == null || typeNames.stream().noneMatch(Objects::nonNull) || typeNames.stream().noneMatch(StringUtils::isNotEmpty)) {
      return null;
    }
    return typeNames.stream().filter(Objects::nonNull)
      .filter(StringUtils::isNotEmpty)
      .mapToInt(ct -> driverTypes.get(ct).getBitmask()).sum();
  }

  public Set<CityDriverType> getByCityAndBitmask(long cityId, Integer bitmask) {
    if (bitmask == null) {
      return Collections.emptySet();
    }
    final List<CityDriverType> types = this.cityDriverTypes.get(cityId);
    if (types == null) {
      return Collections.emptySet();
    }
    return types.stream()
      .filter(cdt -> (cdt.getBitmask() & bitmask) > 0)
      .collect(Collectors.toSet());
  }

  public Set<String> fromBitMask(final Integer bitmask) {
    Set<String> ret = Sets.newHashSet();
    if (bitmask == null) {
      return ret;
    }
    int bitmaskCopy = bitmask;
    while (bitmaskCopy != 0) {
      int curr = Integer.lowestOneBit(bitmaskCopy);
      DriverType driverType = driverTypesByMask.get(curr);
      if (driverType != null) {
        ret.add(driverType.getName());
      }
      bitmaskCopy ^= curr;
    }
    return ret;
  }

  @PostConstruct
  @Override
  public void refreshCache() {
    Map newDriverTypes = Maps.newHashMap();
    Map newDriverTypesByMask = Maps.newHashMap();
    List<DriverType> cts = driverTypeDslRepository.getAllEnabledOrdered();
    driverTypeDslRepository.getAllEnabledOrdered()
      .forEach(ct -> {
        newDriverTypes.put(ct.getName(), ct);
        newDriverTypesByMask.put(ct.getBitmask(), ct);
        }
      );

    Map newCityDriverMap = cts.stream().flatMap(ct -> ct.getCityDriverTypes().stream())
      .collect(Collectors.groupingBy(
        CityDriverType::getCityId,
        HashMap::new,
        Collectors.toList()
      ));

    driverTypes = newDriverTypes;
    driverTypesByMask = newDriverTypesByMask;
    cityDriverTypes = newCityDriverMap;

    DriverTypeUtils.setDriverTypeCache(this);
  }

  public List<CityDriverType> getCityDriverTypes(Long cityId) {
    return cityDriverTypes.get(cityId);
  }

  @Override
  public Map getAllCacheItems() {
    return driverTypes;
  }

  @Override
  public String getCacheName() {
    return DRIVER_TYPES_CACHE;
  }
}