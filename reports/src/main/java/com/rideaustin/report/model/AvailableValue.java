package com.rideaustin.report.model;

import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AvailableValue {

  private final String title;
  private final String value;

  public AvailableValue(Map.Entry<Long, String> entry) {
    this(entry.getValue(), String.valueOf(entry.getKey()));
  }
}
