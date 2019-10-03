package com.rideaustin.test.fixtures;

import java.util.HashSet;
import java.util.Set;

import com.rideaustin.model.surgepricing.SurgeArea;
import com.rideaustin.model.surgepricing.SurgeFactor;

public class SurgeAreaFixture extends AbstractFixture<SurgeArea> {

  private final AreaGeometryFixture areaGeometryFixture;
  private final Set<SurgeFactorFixture> factors = new HashSet<>();

  public SurgeAreaFixture(AreaGeometryFixture areaGeometryFixture) {
    this.areaGeometryFixture = areaGeometryFixture;
  }

  @Override
  protected SurgeArea createObject() {
    return SurgeArea.builder()
      .automated(false)
      .active(true)
      .cityId(1L)
      .carCategoriesBitmask(23)
      .name("Surge area")
      .areaGeometry(areaGeometryFixture.getFixture())
      .build();
  }

  public void addFactor(SurgeFactorFixture fixture) {
    factors.add(fixture);
  }

  @Override
  public SurgeArea getFixture() {
    SurgeArea area = createObject();
    area = entityManager.merge(area);
    Set<SurgeFactor> surgeFactors = new HashSet<>();
    for (SurgeFactorFixture factor : factors) {
      factor.setArea(area);
      surgeFactors.add(factor.getFixture());
    }
    area.setSurgeFactors(surgeFactors);
    area = entityManager.merge(area);
    entityManager.flush();
    return area;
  }
}
