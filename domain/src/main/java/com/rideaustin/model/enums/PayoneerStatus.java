package com.rideaustin.model.enums;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;

public enum PayoneerStatus {
  INITIAL("Not registered"),
  PENDING("Inactive"),
  ACTIVE("Active");

  private final String status;

  private static final Map<String, PayoneerStatus> NAMES = ImmutableMap.of(
    "not registered", INITIAL,
    "initial", INITIAL,
    "inactive", PENDING,
    "pending", PENDING,
    "active", ACTIVE
  );

  PayoneerStatus(String status) {
    this.status = status;
  }

  @JsonCreator
  public static PayoneerStatus from(String payoneerResponse) {
    return NAMES.get(payoneerResponse.toLowerCase());
  }

  @JsonValue
  public String getStatus() {
    return status;
  }
}
