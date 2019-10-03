package com.rideaustin.service.eligibility.checks;

import static com.rideaustin.service.eligibility.checks.EligibilityCheckItem.Order.FIRST_ORDER;

import java.util.Collections;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.rideaustin.model.ride.Car;
import com.rideaustin.service.eligibility.EligibilityCheck;
import com.rideaustin.service.eligibility.EligibilityCheckError;

@Component
@EligibilityCheck(targetClass = Car.class)
public class CarIsSelectedEligibilityCheck extends BaseEligibilityCheckItem<Car> {

  static final String MESSAGE = "You need to specify which car you are driving to become active";

  public CarIsSelectedEligibilityCheck() {
    super(Collections.emptyMap());
  }

  @Override
  public Optional<EligibilityCheckError> check(Car subject) {
    if (subject == null) {
      return Optional.of(new EligibilityCheckError(MESSAGE));
    }
    return Optional.empty();
  }

  @Override
  public int getOrder() {
    return FIRST_ORDER;
  }
}
