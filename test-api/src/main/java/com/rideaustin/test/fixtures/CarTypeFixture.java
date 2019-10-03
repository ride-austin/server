package com.rideaustin.test.fixtures;

import com.rideaustin.model.ride.CarType;

public class CarTypeFixture extends AbstractFixture<CarType> {

  private String carCategory;

  CarTypeFixture(String carCategory) {
    this.carCategory = carCategory;
  }

  public static CarTypeFixtureBuilder builder() {
    return new CarTypeFixtureBuilder();
  }

  @Override
  protected CarType createObject() {
    return entityManager.find(CarType.class, carCategory);
  }

  public static class CarTypeFixtureBuilder {
    private String carCategory;

    public CarTypeFixture.CarTypeFixtureBuilder carCategory(String carCategory) {
      this.carCategory = carCategory;
      return this;
    }

    public CarTypeFixture build() {
      return new CarTypeFixture(carCategory);
    }

  }
}
