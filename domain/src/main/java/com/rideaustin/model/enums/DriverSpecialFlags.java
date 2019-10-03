package com.rideaustin.model.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public enum DriverSpecialFlags {

  DEAF(1);

  private final int bitmask;

  DriverSpecialFlags(int bitmask) {
    this.bitmask = bitmask;
  }

  public int getBitmask() {
    return bitmask;
  }

  public static Integer toBitMask(Set<DriverSpecialFlags> source) {
    int result = 0;
    for (DriverSpecialFlags flag : source) {
      result |= flag.bitmask;
    }
    return result;
  }

  public static Set<DriverSpecialFlags> fromBitmask(Integer bitmask) {
    if (bitmask == null) {
      return Collections.emptySet();
    }
    return Arrays.stream(values()).filter(v -> (v.bitmask & bitmask) == 1).collect(Collectors.toSet());
  }
}
