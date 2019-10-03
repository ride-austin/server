package com.rideaustin.service.onboarding;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import com.rideaustin.model.enums.PayoneerStatus;
import com.rideaustin.rest.model.DriverOnboardingInfo;

@Component
public class PayoneerOnboardingStatusCheck extends BaseDriverOnboardingStatusCheck<PayoneerStatus> {
  @Override
  protected Set<PayoneerStatus> pendingValues() {
    return EnumSet.of(PayoneerStatus.INITIAL, PayoneerStatus.PENDING);
  }

  @Override
  protected Set<PayoneerStatus> finalReviewValues() {
    return EnumSet.of(PayoneerStatus.ACTIVE);
  }

  @Override
  protected Set<PayoneerStatus> terminalValues() {
    return EnumSet.of(PayoneerStatus.ACTIVE);
  }

  @Override
  protected Supplier<PayoneerStatus> value(DriverOnboardingInfo driver) {
    return driver::getPayoneerStatus;
  }
}
