package com.rideaustin.service.onboarding;

import java.util.List;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.rideaustin.model.enums.CarInspectionStatus;
import com.rideaustin.model.enums.DriverOnboardingStatus;
import com.rideaustin.model.ride.Car;

public class CarInspectionOnboardingStatusCheckTest extends BaseOnboardingStatusCheckTest<Car> {

  @Test
  public void testCheckSetsPending() {
    List<Triple<Car, Car, OnboardingStatusCheck.Result>> data = ImmutableList.of(
      ImmutableTriple.of(createCar(CarInspectionStatus.APPROVED), createCar(CarInspectionStatus.PENDING), OnboardingStatusCheck.Result.PENDING),
      ImmutableTriple.of(createCar(CarInspectionStatus.APPROVED), createCar(CarInspectionStatus.NOT_INSPECTED), OnboardingStatusCheck.Result.PENDING)
    );

    assertResultOnUpdate(data);
  }

  @Test
  public void testCheckSetsFinalReview() {
    List<Triple<Car, Car, OnboardingStatusCheck.Result>> data = ImmutableList.of(
      ImmutableTriple.of(createCar(CarInspectionStatus.PENDING), createCar(CarInspectionStatus.APPROVED), OnboardingStatusCheck.Result.FINAL_REVIEW),
      ImmutableTriple.of(createCar(CarInspectionStatus.NOT_INSPECTED), createCar(CarInspectionStatus.APPROVED), OnboardingStatusCheck.Result.FINAL_REVIEW),
      ImmutableTriple.of(createCar(CarInspectionStatus.REJECTED), createCar(CarInspectionStatus.APPROVED), OnboardingStatusCheck.Result.FINAL_REVIEW)
    );

    assertResultOnUpdate(data);
  }

  @Test
  public void testCheckSetsNotChanged() {
    List<Triple<Car, Car, OnboardingStatusCheck.Result>> data = ImmutableList.of(
      ImmutableTriple.of(createCar(CarInspectionStatus.PENDING), createCar(CarInspectionStatus.PENDING), OnboardingStatusCheck.Result.NOT_CHANGED),
      ImmutableTriple.of(createCar(CarInspectionStatus.NOT_INSPECTED), createCar(CarInspectionStatus.NOT_INSPECTED), OnboardingStatusCheck.Result.NOT_CHANGED),
      ImmutableTriple.of(createCar(CarInspectionStatus.REJECTED), createCar(CarInspectionStatus.REJECTED), OnboardingStatusCheck.Result.NOT_CHANGED),
      ImmutableTriple.of(createCar(CarInspectionStatus.APPROVED), createCar(CarInspectionStatus.APPROVED), OnboardingStatusCheck.Result.NOT_CHANGED)
    );

    assertResultOnUpdate(data);
  }

  @Test
  public void shouldReturnNotChangedOnTerminalStateWithActiveOnboarding() {
    List<Triple<Car, Car, OnboardingStatusCheck.Result>> data = ImmutableList.of(
      ImmutableTriple.of(createCar(CarInspectionStatus.PENDING), createCar(CarInspectionStatus.APPROVED), OnboardingStatusCheck.Result.NOT_CHANGED),
      ImmutableTriple.of(createCar(CarInspectionStatus.NOT_INSPECTED), createCar(CarInspectionStatus.APPROVED), OnboardingStatusCheck.Result.NOT_CHANGED),
      ImmutableTriple.of(createCar(CarInspectionStatus.REJECTED), createCar(CarInspectionStatus.APPROVED), OnboardingStatusCheck.Result.NOT_CHANGED)
    );

    doTest(data, new OnboardingStatusCheck.Context(DriverOnboardingStatus.ACTIVE, null));
  }

  @Override
  protected OnboardingStatusCheck<Car, OnboardingStatusCheck.Context> getCheck() {
    return new CarInspectionOnboardingStatusCheck();
  }
}