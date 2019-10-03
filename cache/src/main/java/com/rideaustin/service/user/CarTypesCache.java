package com.rideaustin.service.user;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.rideaustin.application.cache.CacheItem;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.ride.CityCarType;
import com.rideaustin.repo.dsl.CarTypeDslRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CarTypesCache implements CacheItem {

  private static final String CAR_TYPES_CACHE = "carTypesCache";
  private final CarTypeDslRepository carTypeDslRepository;
  private Map<Long, List<CityCarType>> cityCarTypes = Maps.newHashMap();
  private Map<String, CarType> carTypes = Maps.newHashMap();
  private Map<Integer, CarType> carTypesByMask = Maps.newHashMap();

  public CarType getCarType(final String carCategory) {
    return Optional.ofNullable(carTypes.get(carCategory))
      .orElseGet(() -> carTypeDslRepository.findByCategory(carCategory));
  }

  public Map<String, CarType> getActiveCarTypes() {
    return carTypes;
  }

  public int toBitMask(Collection<String> carCategories) {
    return carCategories.stream().filter(Objects::nonNull)
      .mapToInt(ct -> carTypes.get(ct).getBitmask()).sum();
  }

  public Set<String> fromBitMask(final int bitmask) {
    Set<String> ret = Sets.newHashSet();
    int bitmaskCopy = bitmask;
    while (bitmaskCopy != 0) {
      int curr = Integer.lowestOneBit(bitmaskCopy);
      CarType carType = carTypesByMask.get(curr);
      if (carType != null) {
        ret.add(carType.getCarCategory());
      }
      bitmaskCopy ^= curr;
    }
    return ret;
  }

  @PostConstruct
  @Override
  public void refreshCache() {
    Map newCarTypes = Maps.newHashMap();
    Map newCarTypesByMask = Maps.newHashMap();
    Map newCityCarTypes;
    List<CarType> cts = carTypeDslRepository.getAllOrdered();
    cts.forEach(ct -> {
        newCarTypes.put(ct.getCarCategory(), ct);
        newCarTypesByMask.put(ct.getBitmask(), ct);
      }
    );

    newCityCarTypes = cts.stream().flatMap(ct -> ct.getCityCarTypes().stream())
      .collect(Collectors.groupingBy(
        CityCarType::getCityId,
        HashMap::new,
        Collectors.toList()
      ));

    carTypes = newCarTypes;
    carTypesByMask = newCarTypesByMask;
    cityCarTypes = newCityCarTypes;

    CarTypesUtils.setCarTypesCache(this);
  }

  public List<CityCarType> getCityCarTypes(Long cityId) {
    return cityCarTypes.getOrDefault(cityId, Collections.emptyList());
  }

  public CityCarType getCityCarType(Long cityId, String carCategory) {
    for (CityCarType cityCarType : cityCarTypes.get(cityId)) {
      if (cityCarType.getCarType().getCarCategory().equals(carCategory)) {
        return cityCarType;
      }
    }
    return null;
  }

  @Override
  public Map getAllCacheItems() {
    return ImmutableMap.of("carTypes", carTypes, "carTypesByMask", carTypesByMask, "cityCarTypes", cityCarTypes );
  }

  @Override
  public String getCacheName() {
    return CAR_TYPES_CACHE;
  }
}
