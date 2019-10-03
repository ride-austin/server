package com.rideaustin.service.onboarding;

import java.util.List;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.rideaustin.model.enums.DriverOnboardingStatus;
import com.rideaustin.model.enums.PayoneerStatus;
import com.rideaustin.model.user.Driver;
import com.rideaustin.rest.model.DriverOnboardingInfo;

public class PayoneerOnboardingStatusCheckTest extends BaseOnboardingStatusCheckTest<DriverOnboardingInfo> {
  @Test
  public void testCheckSetsPending() throws Exception {
    List<Triple<DriverOnboardingInfo, DriverOnboardingInfo, OnboardingStatusCheck.Result>> data = ImmutableList.of(
      ImmutableTriple.of(createDriver(PayoneerStatus.ACTIVE), createDriver(PayoneerStatus.INITIAL), OnboardingStatusCheck.Result.PENDING),
      ImmutableTriple.of(createDriver(PayoneerStatus.ACTIVE), createDriver(PayoneerStatus.PENDING), OnboardingStatusCheck.Result.PENDING)
    );

    assertResultOnUpdate(data);
  }

  @Test
  public void testCheckSetsFinalReview() throws Exception {
    List<Triple<DriverOnboardingInfo, DriverOnboardingInfo, OnboardingStatusCheck.Result>> data = ImmutableList.of(
      ImmutableTriple.of(createDriver(PayoneerStatus.INITIAL), createDriver(PayoneerStatus.ACTIVE), OnboardingStatusCheck.Result.FINAL_REVIEW),
      ImmutableTriple.of(createDriver(PayoneerStatus.PENDING), createDriver(PayoneerStatus.ACTIVE), OnboardingStatusCheck.Result.FINAL_REVIEW)
    );

    assertResultOnUpdate(data);
  }

  @Test
  public void shouldReturnNotChangedOnTerminalStateWithActiveOnboarding() throws Exception {
    List<Triple<DriverOnboardingInfo, DriverOnboardingInfo, OnboardingStatusCheck.Result>> data = ImmutableList.of(
      ImmutableTriple.of(createDriver(PayoneerStatus.INITIAL), createDriver(PayoneerStatus.ACTIVE), OnboardingStatusCheck.Result.NOT_CHANGED),
      ImmutableTriple.of(createDriver(PayoneerStatus.PENDING), createDriver(PayoneerStatus.ACTIVE), OnboardingStatusCheck.Result.NOT_CHANGED)
    );

    doTest(data, new OnboardingStatusCheck.Context(DriverOnboardingStatus.ACTIVE, null));
  }

  @Test
  public void testCheckSetsNotChanged() throws Exception {
    List<Triple<DriverOnboardingInfo, DriverOnboardingInfo, OnboardingStatusCheck.Result>> data = ImmutableList.of(
      ImmutableTriple.of(createDriver(PayoneerStatus.INITIAL), createDriver(PayoneerStatus.INITIAL), OnboardingStatusCheck.Result.NOT_CHANGED),
      ImmutableTriple.of(createDriver(PayoneerStatus.PENDING), createDriver(PayoneerStatus.PENDING), OnboardingStatusCheck.Result.NOT_CHANGED),
      ImmutableTriple.of(createDriver(PayoneerStatus.ACTIVE), createDriver(PayoneerStatus.ACTIVE), OnboardingStatusCheck.Result.NOT_CHANGED)
    );

    assertResultOnUpdate(data);
  }

  @Override
  protected OnboardingStatusCheck<DriverOnboardingInfo, OnboardingStatusCheck.Context> getCheck() {
    return new PayoneerOnboardingStatusCheck();
  }

  private Driver createDriver(PayoneerStatus status) {
    return createDriver(status, Driver::setPayoneerStatus);
  }
}