package com.rideaustin.test.utils;

import java.util.UUID;

import com.rideaustin.utils.RandomString;

public class RandomUtils {

  private static final String DEFAULT_COUNTRY_CODE = "1";

  private static final String DEFAULT_DOMAIN = "user.com";

  private static final String DEFAULT_PREFIX = "rider";

  private static final int DEFAULT_NAME_LENGTH = 6;

  public static String randomPhoneNumber() {
    return randomPhoneNumber(DEFAULT_COUNTRY_CODE);
  }

  public static String randomPhoneNumber(String countryCode) {
    return String.format("+%s%s", countryCode, RandomString.generate("1234567980", 10));
  }

  public static String randomEmail() {
    return randomEmail(DEFAULT_PREFIX);
  }

  public static String randomEmail(String prefix) {
    return randomEmail(prefix, DEFAULT_DOMAIN);
  }

  public static String randomEmail(String prefix, String domain) {
    return String.format("%s-%s@%s", prefix, UUID.randomUUID().toString(), domain);
  }

  public static String randomName() {
    return RandomString.generate(DEFAULT_NAME_LENGTH);
  }

}
