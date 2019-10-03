package com.rideaustin.report.enums;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AirportToReport {

  ALL("All"),
  AUSTIN_BERGSTROM("Austin Bergstrom"),
  HOUSTON_GEORGE_BUSH("Houston George Bush"),
  HOUSTON_WILLIAM_P_HOBBY("Houston William P. Hobby");

  private final String name;

  AirportToReport(String name) {
    this.name = name;
  }

  @JsonValue
  public String getName() {
    return name;
  }

  @JsonCreator
  public AirportToReport from(String value) {
    return Arrays.stream(values()).filter(v -> value.equals(v.getName())).findFirst().orElse(null);
  }

  @Override
  public String toString() {
    return name;
  }
}
