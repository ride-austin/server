package com.rideaustin.service.onboarding;

import static com.rideaustin.service.onboarding.OnboardingStatusCheck.Result.FINAL_REVIEW;
import static com.rideaustin.service.onboarding.OnboardingStatusCheck.Result.NOT_CHANGED;
import static com.rideaustin.service.onboarding.OnboardingStatusCheck.Result.PENDING;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import com.rideaustin.model.enums.DriverOnboardingStatus;

public abstract class BaseOnboardingStatusCheck<T, E, C extends OnboardingStatusCheck.Context> implements OnboardingStatusCheck<E, C> {

  protected abstract Set<T> pendingValues();

  protected abstract Set<T> finalReviewValues();

  protected abstract Set<T> terminalValues();

  protected abstract Supplier<T> value(E subject);

  @Override
  public OnboardingStatusCheck.Result check(E old, E updated, C context) {
    T oldValue = Optional.ofNullable(old).map(o -> value(old).get()).orElse(null);
    T newValue = value(updated).get();
    if (!Objects.equals(oldValue, newValue)) {
      return getResult(newValue, context);
    }
    return NOT_CHANGED;
  }

  @Override
  public OnboardingStatusCheck.Result currentValue(E item, C context) {
    return getResult(value(item).get(), context);
  }

  private OnboardingStatusCheck.Result getResult(T status, C context) {
    if (terminalValues().contains(status) && context.getCurrentOnboardingStatus() == DriverOnboardingStatus.ACTIVE) {
      return NOT_CHANGED;
    } else if (pendingValues().contains(status)) {
      return PENDING;
    } else if (finalReviewValues().contains(status)) {
      return FINAL_REVIEW;
    }
    return NOT_CHANGED;
  }
}
