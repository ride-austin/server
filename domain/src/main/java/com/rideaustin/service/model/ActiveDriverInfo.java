package com.rideaustin.service.model;

import java.util.Set;

import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.ride.Car;
import com.rideaustin.model.user.Driver;
import com.rideaustin.service.user.CarTypesUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class ActiveDriverInfo {

  private final long id;
  @Setter
  private int availableCarCategoriesBitmask;
  @Setter
  private int availableDriverTypesBitmask;
  private final Long cityId;
  private final Driver driver;
  private final Car car;
  private final ActiveDriverStatus status;

  public ActiveDriverInfo(long id, Driver driver, Car car, Long cityId) {
    this.id = id;
    this.driver = driver;
    this.car = car;
    this.cityId = cityId;
    this.status = ActiveDriverStatus.AVAILABLE;
  }

  public Set<String> getCarCategories() {
    return CarTypesUtils.fromBitMask(availableCarCategoriesBitmask);
  }
}
