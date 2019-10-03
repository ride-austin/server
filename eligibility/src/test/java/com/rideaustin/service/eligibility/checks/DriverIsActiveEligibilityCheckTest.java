package com.rideaustin.service.eligibility.checks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.rideaustin.model.enums.DriverActivationStatus;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.User;
import com.rideaustin.service.eligibility.EligibilityCheckError;

public class DriverIsActiveEligibilityCheckTest {

  private DriverIsActiveEligibilityCheck testedInstance;

  @Before
  public void setUp() throws Exception {
    testedInstance = new DriverIsActiveEligibilityCheck();
  }

  @Test
  public void testCheckReturnsNoErrorIfUserAndDriverAreEnabled() throws Exception {
    Driver driver = new Driver();
    User user = new User();
    user.setUserEnabled(true);
    driver.setUser(user);
    driver.setActive(true);
    driver.setActivationStatus(DriverActivationStatus.ACTIVE);

    Optional<EligibilityCheckError> result = testedInstance.check(driver);

    assertFalse(result.isPresent());
  }

  @Test
  public void testCheckReturnsNoErrorIfDriverIsNull() {
    Optional<EligibilityCheckError> result = testedInstance.check(null);

    assertFalse(result.isPresent());
  }

  @Test
  public void testCheckReturnsErrorIfUserIsDisabled() {
    Driver driver = new Driver();
    User user = new User();
    user.setUserEnabled(false);
    driver.setUser(user);
    driver.setActive(true);

    Optional<EligibilityCheckError> result = testedInstance.check(driver);

    assertTrue(result.isPresent());
    assertEquals(DriverIsActiveEligibilityCheck.MESSAGE, result.get().getMessage());
  }

  @Test
  public void testCheckReturnsErrorIfDriverIsInactive() {
    Driver driver = new Driver();
    User user = new User();
    user.setUserEnabled(true);
    driver.setUser(user);
    driver.setActive(false);

    Optional<EligibilityCheckError> result = testedInstance.check(driver);

    assertTrue(result.isPresent());
    assertEquals(DriverIsActiveEligibilityCheck.MESSAGE, result.get().getMessage());
  }

  @Test
  public void testCheckReturnsErrorIfDriverIsInInactiveActivationStatus() {
    Driver driver = new Driver();
    User user = new User();
    user.setUserEnabled(true);
    driver.setUser(user);
    driver.setActive(true);
    driver.setActivationStatus(DriverActivationStatus.SUSPENDED);

    Optional<EligibilityCheckError> result = testedInstance.check(driver);

    assertTrue(result.isPresent());
    assertEquals(DriverIsActiveEligibilityCheck.MESSAGE, result.get().getMessage());
  }

}