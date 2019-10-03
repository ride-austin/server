package com.rideaustin.service.onboarding;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import com.rideaustin.model.enums.DriverActivationStatus;
import com.rideaustin.rest.model.DriverOnboardingInfo;

@Component
public class ActivationOnboardingStatusCheck extends BaseDriverOnboardingStatusCheck<DriverActivationStatus> {

  @Override
  protected Set<DriverActivationStatus> pendingValues() {
    return EnumSet.of(DriverActivationStatus.DEACTIVATED_OTHER, DriverActivationStatus.INACTIVE);
  }

  @Override
  protected Set<DriverActivationStatus> finalReviewValues() {
    return EnumSet.of(DriverActivationStatus.ACTIVE);
  }

  @Override
  protected Set<DriverActivationStatus> terminalValues() {
    return EnumSet.of(DriverActivationStatus.ACTIVE);
  }

  @Override
  protected Supplier<DriverActivationStatus> value(DriverOnboardingInfo subject) {
    return subject::getActivationStatus;
  }

  @Override
  public OnboardingStatusCheck.Result check(DriverOnboardingInfo old, DriverOnboardingInfo updated, Context context) {
    DriverActivationStatus oldValue = value(old).get();
    DriverActivationStatus newValue = value(updated).get();
    OnboardingStatusCheck.Result currentStatus = context.getCurrentCheckResult();
    if (oldValue == newValue && currentStatus != OnboardingStatusCheck.Result.FINAL_REVIEW) {
      return currentStatus;
    }
    switch (newValue) {
      case SUSPENDED:
        return OnboardingStatusCheck.Result.SUSPENDED;
      case REJECTED:
        return OnboardingStatusCheck.Result.REJECTED;
      case ACTIVE:
        if (old.getActivationStatus() != newValue) {
          return OnboardingStatusCheck.Result.ACTIVE;
        }
        return currentStatus;
      case DEACTIVATED_OTHER:
        return OnboardingStatusCheck.Result.PENDING;
      case INACTIVE:
        return currentStatus;
    }
    return currentStatus;
  }

  @Override
  public OnboardingStatusCheck.Result currentValue(DriverOnboardingInfo item, Context context) {
    return context.getCurrentCheckResult();
  }

  @Override
  public int getOrder() {
    return 0;
  }
}
