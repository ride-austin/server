package com.rideaustin.service.onboarding;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import com.rideaustin.model.enums.DriverOnboardingStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

public interface OnboardingStatusCheck<E, C> {

  Result check(E old, E updated, C context);

  Result currentValue(E item, C context);

  boolean supports(Class<?> clazz);

  default int getOrder() {
    return Integer.MIN_VALUE;
  }

  @AllArgsConstructor
  @Getter
  class Context {
    private final DriverOnboardingStatus currentOnboardingStatus;
    private final Result currentCheckResult;
  }

  enum Result {
    PENDING(DriverOnboardingStatus.PENDING),
    FINAL_REVIEW(DriverOnboardingStatus.FINAL_REVIEW),
    REJECTED(DriverOnboardingStatus.REJECTED),
    SUSPENDED(DriverOnboardingStatus.SUSPENDED),
    ACTIVE(DriverOnboardingStatus.ACTIVE),
    NOT_CHANGED(null);

    public static final Set<Result> TERMINAL_STATUSES = Collections.unmodifiableSet(EnumSet.of(ACTIVE, PENDING, REJECTED, SUSPENDED));

    private final DriverOnboardingStatus onboardingStatus;

    Result(DriverOnboardingStatus onboardingStatus) {
      this.onboardingStatus = onboardingStatus;
    }

    public DriverOnboardingStatus status() {
      return onboardingStatus;
    }
  }
}
