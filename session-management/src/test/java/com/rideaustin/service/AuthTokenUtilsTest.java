package com.rideaustin.service;

import static org.junit.Assert.*;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;

import com.rideaustin.model.user.User;

public class AuthTokenUtilsTest {

  @Test
  public void generateAuthTokenProducesToken() {
    final String result = AuthTokenUtils.generateAuthToken();

    assertNotNull(result);
  }

  @Test
  public void generateAuthTokenProducesTokenForUser() {
    final User user = new User();
    user.setId(1L);

    final String result = AuthTokenUtils.generateAuthToken(user);

    assertTrue(result.startsWith("1:"));
  }

  @Test
  public void calculateTokenExpirationTimeForMobile() {
    Date plus500years = DateUtils.addHours(new Date(), 4380000);

    final Date result = AuthTokenUtils.calculateTokenExpirationTime(true);

    System.out.println(result.getTime());
    System.out.println(plus500years.getTime());
    assertTrue(Math.abs(result.getTime() - plus500years.getTime()) < 1000);
  }

  @Test
  public void calculateTokenExpirationTimeForWeb() {
    Date plus1Hour = DateUtils.addHours(new Date(), 1);

    final Date result = AuthTokenUtils.calculateTokenExpirationTime(false);

    assertTrue(Math.abs(result.getTime() - plus1Hour.getTime()) < 1000);
  }
}