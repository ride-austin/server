package com.rideaustin.utils;

public class PhoneNumberUtils {

  private PhoneNumberUtils() {}

  public static String cleanPhoneNumber(String original) {
    return original.replaceAll("[^0-9]", "");
  }

  public static String preponeWithUSCountryCode(String phoneNumber) {
    String preponed = phoneNumber
      .replace(" ", "")
      .replace("(", "")
      .replace(")", "s").trim();

    if(preponed.startsWith("00")){
      preponed = preponed.replaceFirst("00", "+");
    }

    if (preponed.startsWith("+")) {
      return preponed;
    } else if (preponed.startsWith("1")) {
      return "+" + preponed;
    } else {
      return "+1" + preponed;
    }
  }

  public static String onlyLast10Numbers(final String original) {
    String trimmed = original;
    if (original.length() > 10) {
      trimmed = original.substring(original.length() - 10);
    }
    return trimmed;
  }

  public static String toBracketsStandard(final String cleanedPhoneNumber) {
    if (cleanedPhoneNumber == null) {
      return null;
    }
    if (cleanedPhoneNumber.length() < 10) {
      return null;
    }
    String last10Numbers = onlyLast10Numbers(cleanedPhoneNumber);
    return "("
      .concat(last10Numbers.substring(0, 3))
      .concat(") ")
      .concat(last10Numbers.substring(3, 6))
      .concat("-")
      .concat(last10Numbers.substring(6, 10));

  }

}
