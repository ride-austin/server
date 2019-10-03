package com.rideaustin.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class DriverUtilsTest {
  @Test
  public void testFixNameCaseNull() {
    String result = DriverUtils.fixNameCase(null);

    assertNull(result);
  }

  @Test
  public void testFixNameCaseEmpty() {
    String result = DriverUtils.fixNameCase("");

    assertNull(result);
  }

  @Test
  public void testFixNameCaseUpperCase() {
    String result = DriverUtils.fixNameCase("NAME");

    assertEquals("Name", result);
  }

  @Test
  public void testFixNameCaseLowerCase() {
    String result = DriverUtils.fixNameCase("name");

    assertEquals("Name", result);
  }

  @Test
  public void testFixNameCaseNormalCase() {
    String result = DriverUtils.fixNameCase("Name");

    assertEquals("Name", result);
  }

}