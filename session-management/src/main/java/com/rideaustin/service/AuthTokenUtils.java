package com.rideaustin.service;

import java.util.Date;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import com.rideaustin.model.user.User;

@Service
public class AuthTokenUtils {

  private static final int EXPIRATION_TIME_FOR_MOBILE_IN_HOURS = 4380000;  //50 years
  private static final int EXPIRATION_TIME_FOR_WEB_IN_HOURS = 1;

  private AuthTokenUtils() {

  }

  public static String generateAuthToken() {
    return UUID.randomUUID().toString();
  }

  public static String generateAuthToken(User user) {
    return String.format("%d:%s", user.getId(), UUID.randomUUID());
  }

  public static Date calculateTokenExpirationTime(boolean forMobileApp) {
    DateTime dt = new DateTime();
    if (forMobileApp) {
      dt = dt.plusHours(EXPIRATION_TIME_FOR_MOBILE_IN_HOURS);
    } else {
      dt = dt.plusHours(EXPIRATION_TIME_FOR_WEB_IN_HOURS);
    }
    return dt.toDate();
  }
}
