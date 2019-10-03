package com.rideaustin.service.thirdparty.lookup;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PhoneNumberInfo {

  private final String rawNumber;

  private final String countryCode;

  private final PhoneNumberType type;

  private final PhoneNumberStatus status;

  public enum PhoneNumberStatus {
    EXISTENT,
    NON_EXISTENT,
    UNKNOWN
  }

  public enum PhoneNumberType {
    LANDLINE("landline"),
    MOBILE("mobile"),
    VOIP("voip"),
    UNKNOWN("unknown");

    private static Map<String, PhoneNumberType> types;

    static {
      types = Arrays.stream(values()).collect(Collectors.toMap(PhoneNumberType::getName, Function.identity()));
    }

    private final String name;

    PhoneNumberType(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

    public static PhoneNumberType fromValue(String value) {
      if (StringUtils.isBlank(value)) {
        return UNKNOWN;
      }

      return types.get(StringUtils.stripToEmpty(value).toLowerCase());
    }
  }
}
