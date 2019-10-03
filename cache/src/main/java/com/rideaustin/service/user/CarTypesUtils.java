package com.rideaustin.service.user;

import java.util.Set;

import com.rideaustin.model.ride.CarType;

public class CarTypesUtils {

  private static CarTypesCache carTypesCache;

  private CarTypesUtils(){}

  private static CarTypesCache getCacheInstance() {
    if (carTypesCache == null) {
      throw new IllegalStateException("Car categories error");
    }
    return carTypesCache;
  }

  public static CarType getCarType(String carCategory) {
    return getCacheInstance().getCarType(carCategory);
  }

  public static Set<String> fromBitMask(int carCategoriesBitmask) {
    return getCacheInstance().fromBitMask(carCategoriesBitmask);
  }

  public static int toBitMask(Set<String> carCategories) {
    return getCacheInstance().toBitMask(carCategories);
  }

  public static void setCarTypesCache(CarTypesCache carTypesCache) {
    CarTypesUtils.carTypesCache = carTypesCache;
  }
}
