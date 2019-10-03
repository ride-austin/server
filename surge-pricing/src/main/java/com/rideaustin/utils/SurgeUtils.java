package com.rideaustin.utils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.rideaustin.Constants;
import com.rideaustin.model.surgepricing.SurgeFactor;
import com.rideaustin.service.user.CarTypesUtils;

public final class SurgeUtils {

  private SurgeUtils() {}

  public static Map<String, BigDecimal> createSurgeMapping(Collection<SurgeFactor> surgeFactors, int carCategoriesBitmask) {
    Map<String, BigDecimal> surgeMapping = new HashMap<>();
    for (String carCategory : CarTypesUtils.fromBitMask(carCategoriesBitmask)) {
      surgeMapping.put(carCategory, findSurgeFactor(surgeFactors, carCategory)
        .map(SurgeFactor::getValue)
        .orElse(Constants.NEUTRAL_SURGE_FACTOR)
      );
    }
    return surgeMapping;
  }

  public static Optional<SurgeFactor> findSurgeFactor(Collection<SurgeFactor> surgeFactors, String category) {
    return surgeFactors.stream()
      .filter(sf -> category.equals(sf.getCarType()))
      .findFirst();
  }
}
