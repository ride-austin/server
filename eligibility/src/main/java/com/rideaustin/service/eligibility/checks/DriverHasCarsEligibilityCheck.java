package com.rideaustin.service.eligibility.checks;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import com.rideaustin.model.ride.Car;
import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.dsl.CarDslRepository;
import com.rideaustin.service.eligibility.EligibilityCheck;
import com.rideaustin.service.eligibility.EligibilityCheckError;

@Component
@EligibilityCheck(targetClass = Driver.class)
public class DriverHasCarsEligibilityCheck extends BaseEligibilityCheckItem<Driver> {

  static final String MESSAGE = "Driver does not have any cars associated";

  @Inject
  private CarDslRepository carDslRepository;

  public DriverHasCarsEligibilityCheck() {
    super(Collections.emptyMap());
  }

  @Override
  public Optional<EligibilityCheckError> check(Driver subject) {
    if (subject != null) {
      List<Car> driverCars = carDslRepository.findByDriver(subject.getId());
      if (CollectionUtils.isEmpty(driverCars)) {
        return Optional.of(new EligibilityCheckError(MESSAGE));
      }
    }
    return Optional.empty();
  }

  public void setCarDslRepository(CarDslRepository carDslRepository) {
    this.carDslRepository = carDslRepository;
  }
}
