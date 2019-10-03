package com.rideaustin.test.fixtures;

import java.math.BigDecimal;

import com.rideaustin.model.surgepricing.SurgeArea;
import com.rideaustin.model.surgepricing.SurgeFactor;

public class SurgeFactorFixture extends AbstractFixture<SurgeFactor> {

  private final String carType;
  private final BigDecimal value;
  private SurgeArea area;

  private SurgeFactorFixture(String carType, BigDecimal value) {
    this.carType = carType;
    this.value = value;
  }

  @Override
  protected SurgeFactor createObject() {
    return SurgeFactor.builder()
      .carType(carType)
      .value(value)
      .surgeArea(area)
      .build();
  }

  public void setArea(SurgeArea area) {
    this.area = area;
  }

  public static SurgeFactorFixtureBuilder builder() {
    return new SurgeFactorFixtureBuilder();
  }

  public static class SurgeFactorFixtureBuilder {
    private String carType;
    private BigDecimal value = BigDecimal.ONE;

    public SurgeFactorFixtureBuilder carType(String carType) {
      this.carType = carType;
      return this;
    }

    public SurgeFactorFixtureBuilder value(BigDecimal value) {
      this.value = value;
      return this;
    }

    public SurgeFactorFixture build() {
      return new SurgeFactorFixture(carType, value);
    }
  }
}
