package com.rideaustin.service.event.model;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.rideaustin.model.redis.AreaGeometry;
import com.rideaustin.model.surgepricing.SurgeArea;
import com.rideaustin.service.user.CarTypesUtils;
import com.rideaustin.utils.SurgeUtils;

import lombok.Getter;

@Getter
public class SurgeAreaUpdateContent {

  private final long id;
  private final String name;
  @JsonUnwrapped
  private final AreaGeometry areaGeometry;
  private final Set<String> carCategories;
  private final Map<String, BigDecimal> carCategoriesFactors;

  public SurgeAreaUpdateContent(SurgeArea surgeArea) {
    this.id = surgeArea.getId();
    this.name = surgeArea.getName();
    this.areaGeometry = new AreaGeometry(surgeArea.getAreaGeometry());
    this.carCategories = CarTypesUtils.fromBitMask(surgeArea.getCarCategoriesBitmask());
    this.carCategoriesFactors = SurgeUtils.createSurgeMapping(surgeArea.getSurgeFactors(), surgeArea.getCarCategoriesBitmask());
  }

}
