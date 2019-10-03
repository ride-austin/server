package com.rideaustin.service.onboarding;

import org.junit.Test;

import com.rideaustin.model.enums.DriverActivationStatus;
import com.rideaustin.model.user.Driver;
import com.rideaustin.rest.model.DriverOnboardingInfo;

public class ActivationOnboardingStatusCheckTest extends BaseOnboardingStatusCheckTest<DriverOnboardingInfo> {

  @Test
  public void testCheckReturnsSuspendedOnSuspended() throws Exception {
    assertResultOnUpdate(createDriver(DriverActivationStatus.ACTIVE), createDriver(DriverActivationStatus.SUSPENDED),
      OnboardingStatusCheck.Result.SUSPENDED);
  }

  @Test
  public void testCheckReturnsRejectedOnRejected() {
    assertResultOnUpdate(createDriver(DriverActivationStatus.ACTIVE), createDriver(DriverActivationStatus.REJECTED),
      OnboardingStatusCheck.Result.REJECTED);
  }

  @Test
  public void shouldReturnActiveOnActivated() {
    doTest(createDriver(DriverActivationStatus.REJECTED), createDriver(DriverActivationStatus.ACTIVE), OnboardingStatusCheck.Result.ACTIVE);
  }

  @Test
  public void shouldReturnSameOnStillActivated() {
    doTest(createDriver(DriverActivationStatus.ACTIVE), createDriver(DriverActivationStatus.ACTIVE), null);
  }

  @Override
  protected OnboardingStatusCheck<DriverOnboardingInfo, OnboardingStatusCheck.Context> getCheck() {
    return new ActivationOnboardingStatusCheck();
  }

  private Driver createDriver(DriverActivationStatus status) {
    return createDriver(status, Driver::setActivationStatus);
  }
}