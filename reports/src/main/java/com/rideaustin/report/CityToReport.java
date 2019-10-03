package com.rideaustin.report;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CityToReport {
  ALL("All cities"),
  AUSTIN("Austin"),
  HOUSTON("Houston");

  private final String name;

  CityToReport(String name) {
    this.name = name;
  }

  @JsonValue
  public String getName() {
    return name;
  }

  @JsonCreator
  public static CityToReport from(String value) {
    return Arrays.stream(values()).filter(v -> value.equals(v.getName())).findFirst().orElse(null);
  }
}
