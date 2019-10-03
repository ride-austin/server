package com.rideaustin.service.onboarding;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.tuple.Triple;

import com.rideaustin.model.Document;
import com.rideaustin.model.enums.CarInspectionStatus;
import com.rideaustin.model.enums.DocumentStatus;
import com.rideaustin.model.ride.Car;
import com.rideaustin.model.user.Driver;

public abstract class BaseOnboardingStatusCheckTest<E> {

  protected void doTest(E old, E updated, OnboardingStatusCheck.Result expected) {
    doTest(old, updated, expected, new OnboardingStatusCheck.Context(null, null));
  }

  protected void doTest(E old, E updated, OnboardingStatusCheck.Result expected, OnboardingStatusCheck.Context context) {
    OnboardingStatusCheck.Result result = getCheck().check(old, updated, context);
    assertEquals(expected, result);
  }

  protected void doTest(List<Triple<E, E, OnboardingStatusCheck.Result>> data, OnboardingStatusCheck.Context context) {
    for (Triple<E, E, OnboardingStatusCheck.Result> triplet : data) {
      doTest(triplet.getLeft(), triplet.getMiddle(), triplet.getRight(), context);
    }
  }

  protected void assertResultOnUpdate(E old, E updated, OnboardingStatusCheck.Result expected) {
    OnboardingStatusCheck.Result result = getCheck().check(old, updated, new OnboardingStatusCheck.Context(null, null));
    assertEquals(expected, result);
  }

  protected void assertResultOnUpdate(List<Triple<E, E, OnboardingStatusCheck.Result>> data) {
    for (Triple<E, E, OnboardingStatusCheck.Result> triplet : data) {
      assertResultOnUpdate(triplet.getLeft(), triplet.getMiddle(), triplet.getRight());
    }
  }

  protected void assertResultOnCurrentValue(E entity, OnboardingStatusCheck.Result expected) {
    OnboardingStatusCheck.Result result = getCheck().currentValue(entity, new OnboardingStatusCheck.Context(null, null));
    assertEquals(expected, result);
  }

  protected abstract OnboardingStatusCheck<E, OnboardingStatusCheck.Context> getCheck();

  protected <T> Driver createDriver(T status, BiConsumer<Driver, T> setter) {
    Driver driver = new Driver();
    setter.accept(driver, status);
    return driver;
  }

  protected Car createCar(CarInspectionStatus status) {
    Car car = new Car();
    car.setInspectionStatus(status);
    return car;
  }

  protected Document createDocument(DocumentStatus status) {
    Document document = new Document();
    document.setDocumentStatus(status);
    return document;
  }
}
