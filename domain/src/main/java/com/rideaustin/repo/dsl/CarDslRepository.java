package com.rideaustin.repo.dsl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.rideaustin.model.ride.Car;
import com.rideaustin.model.ride.QCar;
import com.rideaustin.model.user.Driver;

@Repository
public class CarDslRepository extends AbstractDslRepository {

  private static final QCar qCar = QCar.car;

  public Car findOne(Long id) {
    return get(id, Car.class);
  }

  public List<Car> findByDriver(Long driverId) {
    return buildQuery(qCar).where(qCar.driver.id.eq(driverId).and(qCar.removed.eq(false))).orderBy(qCar.id.asc()).fetch();
  }

  public Car getSelected(Driver driver) {
    return buildQuery(qCar).where(qCar.selected.eq(Boolean.TRUE).and(qCar.driver.eq(driver)).and(qCar.removed.eq(false))).fetchOne();
  }
}
