package com.rideaustin.utils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rideaustin.service.model.Version;

public class AppInfoUtils {

  private static final Pattern PATTERN = Pattern.compile(".*([0-9]+\\.[0-9]+\\.[0-9]+).*");

  private AppInfoUtils() {}

  public static String extractVersion(String userAgent) {
    return Optional.ofNullable(userAgent)
      .map(x -> x.replaceAll("Ride(Austin|Houston)(Driver)?_", "").replace('_', ' '))
      .orElse("");
  }

  public static Version createVersion(String userAgent) {
    return new Version(extractVersionNumber(userAgent));
  }

  private static String extractVersionNumber(String userAgent) {
    return Optional.ofNullable(userAgent)
      .map(x -> {
        Matcher matcher = PATTERN.matcher(userAgent);
        if (matcher.matches()) {
          return matcher.group(1);
        }
        return "";
      })
      .orElse("");
  }

}