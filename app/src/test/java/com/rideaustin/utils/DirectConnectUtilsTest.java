package com.rideaustin.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DirectConnectUtilsTest {

  @Test
  public void generateNextId() {
    String result = DirectConnectUtils.generateNextId();

    String expected = "10001";

    assertEquals(expected, result);
  }

  @Test
  public void generateNextId10009() {
    String result = DirectConnectUtils.generateNextId("10009");

    String expected = "10010";

    assertEquals(expected, result);
  }

  @Test
  public void generateNextId10099() {
    String result = DirectConnectUtils.generateNextId("10099");

    String expected = "10100";

    assertEquals(expected, result);
  }

  @Test
  public void generateNextId10999() {
    String result = DirectConnectUtils.generateNextId("10999");

    String expected = "11000";

    assertEquals(expected, result);
  }

  @Test
  public void generateNextId19999() {
    String result = DirectConnectUtils.generateNextId("19999");

    String expected = "20000";

    assertEquals(expected, result);
  }

  @Test(expected = IllegalArgumentException.class)
  public void generateNextId99999() {
    DirectConnectUtils.generateNextId("99999");
  }


}