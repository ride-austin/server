package com.rideaustin.service.onboarding;

import com.rideaustin.rest.model.DriverOnboardingInfo;

public abstract class BaseDriverOnboardingStatusCheck<T> extends BaseOnboardingStatusCheck<T, DriverOnboardingInfo, OnboardingStatusCheck.Context> {

  @Override
  public boolean supports(Class<?> clazz) {
    return DriverOnboardingInfo.class.isAssignableFrom(clazz);
  }
}
