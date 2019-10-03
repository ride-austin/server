package com.rideaustin.service.onboarding;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import com.rideaustin.model.enums.CityApprovalStatus;
import com.rideaustin.rest.model.DriverOnboardingInfo;

@Component
public class CityApprovalOnboardingStatusCheck extends BaseDriverOnboardingStatusCheck<CityApprovalStatus> {
  @Override
  protected Set<CityApprovalStatus> pendingValues() {
    return EnumSet.of(CityApprovalStatus.PENDING, CityApprovalStatus.NOT_PROVIDED, CityApprovalStatus.REJECTED_BY_CITY, CityApprovalStatus.REJECTED_PHOTO, CityApprovalStatus.EXPIRED);
  }

  @Override
  protected Set<CityApprovalStatus> finalReviewValues() {
    return EnumSet.of(CityApprovalStatus.APPROVED);
  }

  @Override
  protected Set<CityApprovalStatus> terminalValues() {
    return EnumSet.of(CityApprovalStatus.APPROVED);
  }

  @Override
  protected Supplier<CityApprovalStatus> value(DriverOnboardingInfo driver) {
    return driver::getCityApprovalStatus;
  }
}
