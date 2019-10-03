package com.rideaustin.model.enums;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;

public enum ConfigurationWeekday {
  MONDAY(1, Calendar.MONDAY),
  TUESDAY(2, Calendar.TUESDAY),
  WEDNESDAY(4, Calendar.WEDNESDAY),
  THURSDAY(8, Calendar.THURSDAY),
  FRIDAY(16, Calendar.FRIDAY),
  SATURDAY(32, Calendar.SATURDAY),
  SUNDAY(64, Calendar.SUNDAY),
  ;

  @Getter
  private final int bitmask;
  @Getter
  private final int weekDay;

  ConfigurationWeekday(int bitmask, int weekDay) {
    this.bitmask = bitmask;
    this.weekDay = weekDay;
  }

  public static Set<ConfigurationWeekday> fromBitmask(Integer bitmask) {
    if (bitmask == null) {
      return Collections.emptySet();
    }
    return Arrays.stream(values()).filter(v -> (v.bitmask & bitmask) != 0).collect(Collectors.toSet());
  }

  public static ConfigurationWeekday fromWeekday(int weekday) {
    return Arrays.stream(values()).filter(v -> v.weekDay == weekday).findFirst().orElse(null);
  }
}
