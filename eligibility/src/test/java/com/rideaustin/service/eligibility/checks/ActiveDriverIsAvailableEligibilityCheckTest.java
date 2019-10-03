package com.rideaustin.service.eligibility.checks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableList;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.dsl.ActiveDriverDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.service.eligibility.EligibilityCheckError;

public class ActiveDriverIsAvailableEligibilityCheckTest {

  @Mock
  private RideDslRepository rideDslRepository;
  @Mock
  private ActiveDriverDslRepository activeDriverDslRepository;

  @InjectMocks
  private ActiveDriverIsAvailableEligibilityCheck testedInstance;

  @Before
  public void setUp() throws Exception {
    testedInstance = new ActiveDriverIsAvailableEligibilityCheck();
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testCheckReturnsNoErrorIfActiveDriverIsNotRiding() {
    EnumSet<ActiveDriverStatus> statuses = EnumSet.complementOf(EnumSet.of(ActiveDriverStatus.RIDING));
    for (ActiveDriverStatus status : statuses) {
      ActiveDriver subject = new ActiveDriver();
      subject.setStatus(status);

      Optional<EligibilityCheckError> result = testedInstance.check(subject);

      assertFalse(result.isPresent());
    }
  }

  @Test
  public void testCheckReturnsErrorIfActiveDriverIsRiding() {
    ActiveDriver subject = new ActiveDriver();
    subject.setStatus(ActiveDriverStatus.RIDING);
    when(rideDslRepository.findByActiveDriverAndStatuses(subject, RideStatus.ONGOING_DRIVER_STATUSES)).thenReturn(ImmutableList.of(new Ride()));

    Optional<EligibilityCheckError> result = testedInstance.check(subject);

    assertTrue(result.isPresent());
    assertEquals(ActiveDriverIsAvailableEligibilityCheck.MESSAGE, result.get().getMessage());
  }

}