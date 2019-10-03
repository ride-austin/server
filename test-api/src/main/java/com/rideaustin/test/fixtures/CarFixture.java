package com.rideaustin.test.fixtures;

import com.rideaustin.model.enums.CarInspectionStatus;
import com.rideaustin.model.ride.Car;
import com.rideaustin.model.user.Driver;
import com.rideaustin.utils.RandomString;

public class CarFixture extends AbstractFixture<Car> {

  private int categoriesBitmask;
  private Driver driver;

  @java.beans.ConstructorProperties({"categoriesBitmask", "driver"})
  CarFixture(int categoriesBitmask, Driver driver) {
    this.categoriesBitmask = categoriesBitmask;
    this.driver = driver;
  }

  public static CarFixtureBuilder builder() {
    return new CarFixtureBuilder();
  }

  @Override
  protected Car createObject() {
    Car car = Car.builder()
      .carCategoriesBitmask(categoriesBitmask)
      .color(RandomString.generate())
      .license(RandomString.generate())
      .make(RandomString.generate())
      .model(RandomString.generate())
      .year(RandomString.generate("1234567890", 4))
      .inspectionStatus(CarInspectionStatus.APPROVED)
      .selected(true)
      .removed(false)
      .build();
    car.setDriver(driver);
    return car;
  }

  public int getCategoriesBitmask() {
    return categoriesBitmask;
  }

  public void setDriver(Driver driver) {
    this.driver = driver;
  }

  public static class CarFixtureBuilder {
    private int categoriesBitmask;
    private Driver driver;

    CarFixtureBuilder() {
    }

    public CarFixture.CarFixtureBuilder categoriesBitmask(int categoriesBitmask) {
      this.categoriesBitmask = categoriesBitmask;
      return this;
    }

    public CarFixture.CarFixtureBuilder driver(Driver driver) {
      this.driver = driver;
      return this;
    }

    public CarFixture build() {
      return new CarFixture(categoriesBitmask, driver);
    }

    public String toString() {
      return "com.rideaustin.test.fixtures.CarFixture.CarFixtureBuilder(categoriesBitmask=" + this.categoriesBitmask + ", driver=" + this.driver + ")";
    }
  }
}
