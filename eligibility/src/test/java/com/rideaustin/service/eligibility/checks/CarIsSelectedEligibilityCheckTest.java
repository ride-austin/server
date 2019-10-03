package com.rideaustin.service.eligibility.checks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.rideaustin.model.ride.Car;
import com.rideaustin.service.eligibility.EligibilityCheckError;
import com.rideaustin.service.eligibility.checks.CarIsSelectedEligibilityCheck;

public class CarIsSelectedEligibilityCheckTest {

  private CarIsSelectedEligibilityCheck testedInstance;

  @Before
  public void setUp() throws Exception {
    testedInstance = new CarIsSelectedEligibilityCheck();
  }

  @Test
  public void testCheckReturnNoErrorIfCarIsNotNull() throws Exception {
    Car car = new Car();

    Optional<EligibilityCheckError> result = testedInstance.check(car);

    assertFalse(result.isPresent());
  }

  @Test
  public void testCheckReturnsErrorIfCarIsNull() {
    Optional<EligibilityCheckError> result = testedInstance.check(null);

    assertTrue(result.isPresent());
    assertEquals(CarIsSelectedEligibilityCheck.MESSAGE, result.get().getMessage());
  }

}