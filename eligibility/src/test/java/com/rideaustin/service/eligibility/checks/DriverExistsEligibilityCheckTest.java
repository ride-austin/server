package com.rideaustin.service.eligibility.checks;

import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.rideaustin.model.user.Driver;
import com.rideaustin.service.eligibility.EligibilityCheckError;
import com.rideaustin.service.eligibility.checks.DriverExistsEligibilityCheck;

public class DriverExistsEligibilityCheckTest {

  private DriverExistsEligibilityCheck testedInstance;

  @Before
  public void setUp() throws Exception {
    testedInstance = new DriverExistsEligibilityCheck();
  }

  @Test
  public void testCheckReturnsNoErrorIfDriverIsNotNull() throws Exception {
    Driver driver = new Driver();

    Optional<EligibilityCheckError> result = testedInstance.check(driver);

    assertFalse(result.isPresent());
  }

  @Test
  public void testCheckReturnsErrorIfDriverIsNull() {
    Optional<EligibilityCheckError> result = testedInstance.check(null);

    assertTrue(result.isPresent());
    assertEquals(DriverExistsEligibilityCheck.MESSAGE, result.get().getMessage());
  }

}