package com.rideaustin.service.eligibility.checks;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.rideaustin.model.ride.Car;
import com.rideaustin.service.eligibility.EligibilityCheck;
import com.rideaustin.service.eligibility.DriverEligibilityCheckContext;
import com.rideaustin.service.eligibility.EligibilityCheckError;
import com.rideaustin.service.user.CarTypesCache;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@EligibilityCheck(targetClass = Car.class, contextAware = true)
public class CarCategoryEligibilityCheck extends BaseEligibilityCheckItem<Car> {

  static final String MESSAGE = "Driver not eligible to drive ";

  @Inject
  private CarTypesCache carTypesCache;

  public CarCategoryEligibilityCheck(Map<String, Object> context) {
    super(context);
  }

  @Override
  public Optional<EligibilityCheckError> check(Car subject) {
    Set<String> carCategory = (Set<String>) context.get(DriverEligibilityCheckContext.CAR_CATEGORIES);
    Optional<Car> car = Optional.ofNullable(subject);
    if (car.isPresent() && carTypesCache.fromBitMask(car.get().getCarCategoriesBitmask()).containsAll(carCategory)) {
      return Optional.empty();
    }
    return Optional.of(new EligibilityCheckError(MESSAGE + carCategory));
  }

  public void setCarTypesCache(CarTypesCache carTypesCache) {
    this.carTypesCache = carTypesCache;
  }
}
