package com.rideaustin.service.onboarding;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import com.rideaustin.model.enums.CarInspectionStatus;
import com.rideaustin.model.ride.Car;

@Component
public class CarInspectionOnboardingStatusCheck extends BaseOnboardingStatusCheck<CarInspectionStatus, Car, OnboardingStatusCheck.Context> {
  @Override
  protected Set<CarInspectionStatus> pendingValues() {
    return EnumSet.of(CarInspectionStatus.PENDING, CarInspectionStatus.NOT_INSPECTED, CarInspectionStatus.REJECTED);
  }

  @Override
  protected Set<CarInspectionStatus> finalReviewValues() {
    return EnumSet.of(CarInspectionStatus.APPROVED);
  }

  @Override
  protected Set<CarInspectionStatus> terminalValues() {
    return EnumSet.of(CarInspectionStatus.APPROVED);
  }

  @Override
  protected Supplier<CarInspectionStatus> value(Car car) {
    return car::getInspectionStatus;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return Car.class.isAssignableFrom(clazz);
  }
}
