package com.rideaustin.service.onboarding;

import java.util.List;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.rideaustin.model.enums.CityApprovalStatus;
import com.rideaustin.model.enums.DriverOnboardingStatus;
import com.rideaustin.model.user.Driver;
import com.rideaustin.rest.model.DriverOnboardingInfo;

public class CityApprovalOnboardingStatusCheckTest extends BaseOnboardingStatusCheckTest<DriverOnboardingInfo> {
  @Test
  public void testCheckSetsPending() {
    List<Triple<DriverOnboardingInfo, DriverOnboardingInfo, OnboardingStatusCheck.Result>> data = ImmutableList.of(
      ImmutableTriple.of(createDriver(CityApprovalStatus.APPROVED), createDriver(CityApprovalStatus.PENDING), OnboardingStatusCheck.Result.PENDING),
      ImmutableTriple.of(createDriver(CityApprovalStatus.APPROVED), createDriver(CityApprovalStatus.NOT_PROVIDED), OnboardingStatusCheck.Result.PENDING),
      ImmutableTriple.of(createDriver(CityApprovalStatus.APPROVED), createDriver(CityApprovalStatus.REJECTED_BY_CITY), OnboardingStatusCheck.Result.PENDING),
      ImmutableTriple.of(createDriver(CityApprovalStatus.APPROVED), createDriver(CityApprovalStatus.REJECTED_PHOTO), OnboardingStatusCheck.Result.PENDING),
      ImmutableTriple.of(createDriver(CityApprovalStatus.APPROVED), createDriver(CityApprovalStatus.EXPIRED), OnboardingStatusCheck.Result.PENDING)
    );

    assertResultOnUpdate(data);
  }

  @Test
  public void testCheckSetsFinalReview() {
    List<Triple<DriverOnboardingInfo, DriverOnboardingInfo, OnboardingStatusCheck.Result>> data = ImmutableList.of(
      ImmutableTriple.of(createDriver(CityApprovalStatus.PENDING), createDriver(CityApprovalStatus.APPROVED), OnboardingStatusCheck.Result.FINAL_REVIEW),
      ImmutableTriple.of(createDriver(CityApprovalStatus.NOT_PROVIDED), createDriver(CityApprovalStatus.APPROVED), OnboardingStatusCheck.Result.FINAL_REVIEW),
      ImmutableTriple.of(createDriver(CityApprovalStatus.REJECTED_BY_CITY), createDriver(CityApprovalStatus.APPROVED), OnboardingStatusCheck.Result.FINAL_REVIEW),
      ImmutableTriple.of(createDriver(CityApprovalStatus.REJECTED_PHOTO), createDriver(CityApprovalStatus.APPROVED), OnboardingStatusCheck.Result.FINAL_REVIEW),
      ImmutableTriple.of(createDriver(CityApprovalStatus.EXPIRED), createDriver(CityApprovalStatus.APPROVED), OnboardingStatusCheck.Result.FINAL_REVIEW)
    );

    assertResultOnUpdate(data);
  }

  @Test
  public void shouldReturnNotChangedOnTerminalStateWithActiveOnboarding() {
    List<Triple<DriverOnboardingInfo, DriverOnboardingInfo, OnboardingStatusCheck.Result>> data = ImmutableList.of(
      ImmutableTriple.of(createDriver(CityApprovalStatus.PENDING), createDriver(CityApprovalStatus.APPROVED), OnboardingStatusCheck.Result.NOT_CHANGED),
      ImmutableTriple.of(createDriver(CityApprovalStatus.NOT_PROVIDED), createDriver(CityApprovalStatus.APPROVED), OnboardingStatusCheck.Result.NOT_CHANGED),
      ImmutableTriple.of(createDriver(CityApprovalStatus.REJECTED_BY_CITY), createDriver(CityApprovalStatus.APPROVED), OnboardingStatusCheck.Result.NOT_CHANGED),
      ImmutableTriple.of(createDriver(CityApprovalStatus.REJECTED_PHOTO), createDriver(CityApprovalStatus.APPROVED), OnboardingStatusCheck.Result.NOT_CHANGED),
      ImmutableTriple.of(createDriver(CityApprovalStatus.EXPIRED), createDriver(CityApprovalStatus.APPROVED), OnboardingStatusCheck.Result.NOT_CHANGED)
    );

    doTest(data, new OnboardingStatusCheck.Context(DriverOnboardingStatus.ACTIVE, null));
  }

  @Test
  public void testCheckSetsNotChanged() {
    List<Triple<DriverOnboardingInfo, DriverOnboardingInfo, OnboardingStatusCheck.Result>> data = ImmutableList.of(
      ImmutableTriple.of(createDriver(CityApprovalStatus.PENDING), createDriver(CityApprovalStatus.PENDING), OnboardingStatusCheck.Result.NOT_CHANGED),
      ImmutableTriple.of(createDriver(CityApprovalStatus.NOT_PROVIDED), createDriver(CityApprovalStatus.NOT_PROVIDED), OnboardingStatusCheck.Result.NOT_CHANGED),
      ImmutableTriple.of(createDriver(CityApprovalStatus.REJECTED_BY_CITY), createDriver(CityApprovalStatus.REJECTED_BY_CITY), OnboardingStatusCheck.Result.NOT_CHANGED),
      ImmutableTriple.of(createDriver(CityApprovalStatus.REJECTED_PHOTO), createDriver(CityApprovalStatus.REJECTED_PHOTO), OnboardingStatusCheck.Result.NOT_CHANGED),
      ImmutableTriple.of(createDriver(CityApprovalStatus.EXPIRED), createDriver(CityApprovalStatus.EXPIRED), OnboardingStatusCheck.Result.NOT_CHANGED),
      ImmutableTriple.of(createDriver(CityApprovalStatus.APPROVED), createDriver(CityApprovalStatus.APPROVED), OnboardingStatusCheck.Result.NOT_CHANGED)
    );

    assertResultOnUpdate(data);
  }

  @Override
  protected OnboardingStatusCheck<DriverOnboardingInfo, OnboardingStatusCheck.Context> getCheck() {
    return new CityApprovalOnboardingStatusCheck();
  }

  private Driver createDriver(CityApprovalStatus status) {
    return createDriver(status, Driver::setCityApprovalStatus);
  }
}