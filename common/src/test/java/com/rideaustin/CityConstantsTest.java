package com.rideaustin;

import static org.junit.Assert.*;

import org.junit.Test;

public class CityConstantsTest {

  @Test
  public void getByCityIdReturnsCity() {
    final Constants.City result = Constants.City.getByCityId(1L);

    assertEquals(Constants.City.AUSTIN, result);
  }

  @Test(expected = IllegalArgumentException.class)
  public void getByCityIdThrowsError() {
    final Constants.City result = Constants.City.getByCityId(100L);
  }

}