package com.rideaustin.service.eligibility.checks;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.model.ride.Car;
import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.dsl.CarDslRepository;
import com.rideaustin.service.eligibility.EligibilityCheckError;

public class DriverHasCarsEligibilityCheckTest {

  @Mock
  private CarDslRepository carDslRepository;

  private DriverHasCarsEligibilityCheck testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    testedInstance = new DriverHasCarsEligibilityCheck();
    testedInstance.setCarDslRepository(carDslRepository);
  }

  @Test
  public void testCheckReturnsErrorIfDriverHasNoCars() throws Exception {
    when(carDslRepository.findByDriver(anyLong())).thenReturn(Collections.emptyList());

    Optional<EligibilityCheckError> result = testedInstance.check(new Driver());

    assertTrue(result.isPresent());
    assertEquals(DriverHasCarsEligibilityCheck.MESSAGE, result.get().getMessage());
  }

  @Test
  public void testCheckReturnsNoErrorIfDriverHasCars() throws Exception {
    when(carDslRepository.findByDriver(anyLong())).thenReturn(Collections.singletonList(new Car()));

    Optional<EligibilityCheckError> result = testedInstance.check(new Driver());

    assertFalse(result.isPresent());
  }

  @Test
  public void testCheckReturnsNoErrorIfDriverIsNull() throws Exception {
    Optional<EligibilityCheckError> result = testedInstance.check(null);

    assertFalse(result.isPresent());
  }

}