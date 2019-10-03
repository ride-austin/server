package com.rideaustin.service.user;

import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

public class DriverTypeUtils {

  private static DriverTypeCache driverTypeCache;

  private DriverTypeUtils(){}

  private static DriverTypeCache getCacheInstance() {
    if (driverTypeCache == null) {
      throw new IllegalStateException("Driver types error");
    }
    return driverTypeCache;
  }

  public static Set<String> fromBitMask(Integer driverTypeBitmask) {
    return getCacheInstance().fromBitMask(driverTypeBitmask);
  }

  public static Integer toBitMask(Set<String> carCategories) {
    return getCacheInstance().toBitMask(carCategories);
  }

  public static Integer toBitMask(String commaSeparatedList) {
    return Optional.ofNullable(commaSeparatedList).map(l -> toBitMask(ImmutableSet.copyOf(l.split(",")))).orElse(null);
  }

  public static void setDriverTypeCache(DriverTypeCache driverTypeCache) {
    DriverTypeUtils.driverTypeCache = driverTypeCache;
  }
}
