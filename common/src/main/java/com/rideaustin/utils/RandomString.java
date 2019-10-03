package com.rideaustin.utils;

import java.util.Random;

import javax.annotation.Nonnull;

public class RandomString {
  private static final String CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyz";

  private RandomString() {

  }

  @Nonnull
  public static String generate() {
    return generate(10);
  }

  @Nonnull
  public static String generate(int length) {
    return generate(CHARACTERS, length);
  }

  @Nonnull
  public static String generate(String characters, int length) {
    StringBuilder value = new StringBuilder();
    for (int p = 0; p < length; p++) {
      try {
        int x = new Random().nextInt(characters.length());
        value.append(characters.charAt(x));
      } catch (Exception e) {
        value.append("Z");
        throw e;
      }
    }
    return value.toString();
  }
}