package com.rideaustin.service.eligibility.checks;

import static com.rideaustin.service.eligibility.checks.EligibilityCheckItem.Order.ORDER_DOES_NOT_MATTER;

import java.util.Collections;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.rideaustin.model.City;
import com.rideaustin.model.enums.CarInspectionStatus;
import com.rideaustin.model.ride.Car;
import com.rideaustin.service.CityService;
import com.rideaustin.service.eligibility.EligibilityCheck;
import com.rideaustin.service.eligibility.EligibilityCheckError;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@EligibilityCheck(targetClass = Car.class)
public class CarInspectionStatusEligibilityCheck extends BaseEligibilityCheckItem<Car> {

  private final CityService cityService;

  static final String MESSAGE = "Your vehicle must pass inspection in order to activate it. Please send an email to %s";

  @Inject
  public CarInspectionStatusEligibilityCheck(CityService cityService) {
    super(Collections.emptyMap());
    this.cityService = cityService;
  }

  @Override
  public Optional<EligibilityCheckError> check(Car subject) {
    City city = loadCurrentCity();
    CarInspectionStatus carInspectionStatus = Optional.ofNullable(subject)
      .map(Car::getInspectionStatus)
      .orElse(CarInspectionStatus.NOT_INSPECTED);
    if (carInspectionStatus != CarInspectionStatus.APPROVED) {
      return Optional.of(new EligibilityCheckError(String.format(MESSAGE, city.getDocumentsEmail())));
    }
    return Optional.empty();
  }

  @Override
  public int getOrder() {
    return ORDER_DOES_NOT_MATTER;
  }

  private City loadCurrentCity() {
    return cityService.getCityForCurrentClientAppVersionContext();
  }
}

