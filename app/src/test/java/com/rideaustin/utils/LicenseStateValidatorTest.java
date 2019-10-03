package com.rideaustin.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

@RunWith(DataProviderRunner.class)
public class LicenseStateValidatorTest {

  private LicenseStateValidator testedInstance = new LicenseStateValidator();

  @DataProvider
  public static Object[] eligibleStates() {
    return LicenseStateValidator.STATES.toArray();
  }

  @Test
  @UseDataProvider("eligibleStates")
  public void isValidOkForEligibleStates(String state) {
    assertTrue(testedInstance.isValid(state, null));
  }

  @Test
  public void isValidErrorForRandomString() {
    assertFalse(testedInstance.isValid(RandomString.generate(), null));
  }
}